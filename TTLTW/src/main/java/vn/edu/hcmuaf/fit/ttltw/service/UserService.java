package vn.edu.hcmuaf.fit.ttltw.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import vn.edu.hcmuaf.fit.ttltw.dao.UserDao;
import vn.edu.hcmuaf.fit.ttltw.model.User;

public class UserService {
    private final UserDao userDao;

    // Lockout policy: sai MAX_FAILED_ATTEMPTS lần liên tiếp → khóa LOCK_DURATION_MINUTES phút.
    // Áp dụng cho mọi kênh đăng nhập (form thường, Facebook, Google) — lock theo user, không theo kênh.
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCK_DURATION_MINUTES = 15;

    public UserService() {
        this.userDao = new UserDao();
    }
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-#^()])[A-Za-z\\d@$!%*?&_\\-#^()]{8,}$"
    );
    public Optional<User> getUserProfileById(int id) {
        return userDao.findById(id);
    }

    public Optional<User> getUserProfileByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public int countUsers(String searchTerm, String statusFilter) {
        return userDao.countUsers(searchTerm, statusFilter);
    }

    public List<User> getUsersPaginated(String searchTerm, String statusFilter, int offset, int limit) {
        return userDao.getUsersPaginated(searchTerm, statusFilter, offset, limit);
    }

    public boolean updateUser(int id, int role, int status) {
        return userDao.updateUser(id, role, status);
    }

    public boolean updateUserInfo(int id, String firstName, String lastName, String email) {
        return userDao.updateUserInfo(id, firstName, lastName, email);
    }

    public boolean updateAvatar(int id, String avatarUrl) {
        return userDao.updateAvatar(id, avatarUrl);
    }

    public boolean checkExistEmailForOtherUsers(int id, String email) {
        return userDao.checkExistEmailForOtherUsers(id, email);
    }

    public String updatePassword(int userId, String oldPass, String newPass) {
        if (newPass == null || newPass.isBlank())
            return "Mật khẩu mới không được để trống!";
        if (!PASSWORD_PATTERN.matcher(newPass).matches())
            return "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!";
        boolean checkPassword = userDao.checkPassword(userId, oldPass);
        if (!checkPassword)
            return "Sai mật khẩu";
        String hashed = BCrypt.hashpw(newPass, BCrypt.gensalt());
        boolean ok = userDao.updatePassword(userId, hashed);
        return ok ? "Đổi mật khẩu thành công" : "Đổi mật khẩu thất bại";
    }

    // Admin/nhân viên reset mật khẩu hộ khách hàng (không cần mật khẩu cũ)
    public String resetPasswordByAdmin(int userId, String newPass) {
        if (newPass == null || newPass.isBlank())
            return "Mật khẩu mới không được để trống!";
        if (!PASSWORD_PATTERN.matcher(newPass).matches())
            return "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!";
        String hashed = BCrypt.hashpw(newPass, BCrypt.gensalt());
        boolean ok = userDao.updatePassword(userId, hashed);
        return ok ? "Cập nhật mật khẩu thành công" : "Cập nhật mật khẩu thất bại";
    }

    // Đăng ký (hash password)
    public boolean register(User user) {
        String hash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hash);
        return userDao.register(user);
    }

    // Đăng nhập form thường — trả LoginResult chứa user (nếu thành công) hoặc thông tin về:
    //   - sai mật khẩu (kèm số lần thử còn lại trước khi khóa)
    //   - tài khoản đang bị khóa tạm thời (kèm số giây còn lại)
    //   - tài khoản bị disable vĩnh viễn (status != 1)
    // Hỗ trợ migrate password cũ chưa hash sang BCrypt khi đăng nhập đúng.
    //
    // Thứ tự kiểm tra: lock > password > status. Verify password trước khi phân biệt
    // ACCOUNT_INACTIVE để attacker gửi sai password trên account bị disable cũng chỉ thấy
    // INVALID_CREDENTIALS (giống account không tồn tại) — không leak existence.
    public LoginResult login(String input, String password) {
        User user = userDao.findByInput(input);
        // Không tồn tại → trả invalid để không leak thông tin tài khoản có tồn tại hay không.
        // remaining = MAX để UI hiển thị giống lần sai mật khẩu đầu tiên.
        if (user == null) {
            return LoginResult.invalidCredentials(MAX_FAILED_ATTEMPTS);
        }

        // Tài khoản đang trong thời gian khóa → chặn ngay, không kiểm tra password.
        long lockSeconds = remainingLockSeconds(user.getLockedUntil());
        if (lockSeconds > 0) {
            return LoginResult.accountLocked(lockSeconds);
        }

        boolean passwordOk = verifyAndMaybeMigratePassword(user, password, input);

        // Sai password → tăng counter bất kể status. Account inactive + sai pass trông giống
        // không tồn tại + sai pass (cùng INVALID_CREDENTIALS), không leak.
        if (!passwordOk) {
            return registerFailedAttempt(user.getId());
        }

        // Password đúng nhưng admin đã disable → báo inactive cho user thật biết để liên hệ admin.
        if (user.getStatus() != 1) {
            return LoginResult.accountInactive();
        }

        userDao.resetFailedAttempts(user.getId());
        return LoginResult.success(user);
    }

    private boolean verifyAndMaybeMigratePassword(User user, String password, String input) {
        String storedPassword = user.getPassword();
        if (storedPassword == null) return false;
        storedPassword = storedPassword.trim();

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            if (storedPassword.length() < 59) {
                System.err.println("Invalid hash length: " + storedPassword.length() + " for user " + input);
                return false;
            }
            String hashForCheck = storedPassword;
            if (storedPassword.startsWith("$2y$") || storedPassword.startsWith("$2b$")) {
                hashForCheck = "$2a$" + storedPassword.substring(4);
            }
            try {
                return BCrypt.checkpw(password, hashForCheck);
            } catch (IllegalArgumentException e) {
                System.err.println("BCrypt error for user " + input + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        // Password cũ chưa hash (plain text) — so sánh trực tiếp rồi migrate sang BCrypt
        boolean ok = password != null && password.equals(storedPassword);
        if (ok) {
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            userDao.updatePassword(user.getId(), hashed);
            user.setPassword(hashed);
        }
        return ok;
    }

    // Tăng counter sai cho user và sinh LoginResult tương ứng:
    //   - Nếu vừa đủ ngưỡng → tài khoản bị khóa → trả ACCOUNT_LOCKED
    //   - Nếu chưa đủ ngưỡng → trả INVALID_CREDENTIALS kèm remainingAttempts
    private LoginResult registerFailedAttempt(int userId) {
        int total = userDao.incrementFailedAttempts(userId, MAX_FAILED_ATTEMPTS, LOCK_DURATION_MINUTES);
        if (total >= MAX_FAILED_ATTEMPTS) {
            return LoginResult.accountLocked(LOCK_DURATION_MINUTES * 60L);
        }
        return LoginResult.invalidCredentials(MAX_FAILED_ATTEMPTS - total);
    }

    // Số giây còn lại cho tới khi hết khóa; 0 nghĩa là không bị khóa.
    private long remainingLockSeconds(Timestamp lockedUntil) {
        if (lockedUntil == null) return 0;
        long diffMs = lockedUntil.getTime() - System.currentTimeMillis();
        return diffMs > 0 ? (diffMs + 999) / 1000 : 0;
    }

    public User loginByProvider(String provider, String providerId) {
        return userDao.loginByProvider(provider, providerId);
    }

    // Đăng nhập qua provider (Facebook/Google...).
    // Lưu liên kết social ở bảng user_social_accounts (1 user có thể link nhiều provider).
    // Flow:
    //   1) Đã liên kết provider này trước đó (sa.provider + sa.provider_user_id khớp) -> đăng nhập luôn.
    //   2) Email từ provider đã tồn tại trong hệ thống -> đăng nhập vào account đó
    //      và ghi 1 row vào user_social_accounts để lần sau đi nhánh 1.
    //   3) Chưa có account -> tạo user mới rồi link.
    // Tôn trọng lockout: nếu user tương ứng đang bị khóa tạm thời (do sai mật khẩu nhiều lần
    // ở form thường) thì chặn cả login bằng provider, trả ACCOUNT_LOCKED kèm số giây còn lại.
    public LoginResult loginOrRegisterSocial(User u) {
        // 1. Đã liên kết provider trước đó
        User existed = userDao.loginByProvider(u.getProvider(), u.getProviderId());
        if (existed != null) {
            if (existed.getStatus() != 1) {
                return LoginResult.accountInactive();
            }
            long lockSeconds = remainingLockSeconds(existed.getLockedUntil());
            if (lockSeconds > 0) {
                return LoginResult.accountLocked(lockSeconds);
            }
            userDao.resetFailedAttempts(existed.getId());
            return LoginResult.success(existed);
        }

        String email = u.getEmail();

        // 2. Email tồn tại trong hệ thống -> đăng nhập vào account hiện có
        if (email != null && !email.isBlank()) {
            Optional<User> byEmailOpt = userDao.findByEmail(email);
            if (byEmailOpt.isPresent()) {
                User byEmail = byEmailOpt.get();
                if (byEmail.getStatus() != 1) {
                    return LoginResult.accountInactive();
                }
                long lockSeconds = remainingLockSeconds(byEmail.getLockedUntil());
                if (lockSeconds > 0) {
                    return LoginResult.accountLocked(lockSeconds);
                }
                userDao.linkSocialAccount(byEmail.getId(), u.getProvider(), u.getProviderId(), email);
                userDao.updateAvatarIfEmpty(byEmail.getId(), u.getAvatar());
                userDao.resetFailedAttempts(byEmail.getId());
                return LoginResult.success(byEmail);
            }
        }

        // 3. Tạo tài khoản mới — đảm bảo username không trùng
        String baseUsername = u.getUsername();
        String finalUsername = baseUsername;
        int suffix = 1;
        while (userDao.checkExistUsername(finalUsername)) {
            finalUsername = baseUsername + suffix;
            suffix++;
        }
        u.setUsername(finalUsername);
        int newUserId = userDao.insertSocialUser(u);
        userDao.linkSocialAccount(newUserId, u.getProvider(), u.getProviderId(), email);
        User created = userDao.loginByProvider(u.getProvider(), u.getProviderId());
        if (created == null) {
            return LoginResult.socialLoginFailed();
        }
        return LoginResult.success(created);
    }
}
