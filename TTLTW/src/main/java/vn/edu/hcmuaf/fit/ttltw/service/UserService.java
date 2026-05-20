package vn.edu.hcmuaf.fit.ttltw.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import vn.edu.hcmuaf.fit.ttltw.dao.UserDao;
import vn.edu.hcmuaf.fit.ttltw.model.User;

public class UserService {
    private final UserDao userDao;

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

    // Đăng ký (hash password)
    public boolean register(User user) {
        String hash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hash);
        return userDao.register(user);
    }

    // Đăng nhập (check BCrypt, hỗ trợ migrate password cũ chưa hash)
    public User login(String input, String password) {
        User user = userDao.findByInput(input);
        if (user == null)
            return null;

        String storedPassword = user.getPassword();
        if (storedPassword == null)
            return null;

        // Trim whitespace từ stored password
        storedPassword = storedPassword.trim();

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            // Kiểm tra độ dài hợp lệ của BCrypt hash (phải 60 ký tự)
            if (storedPassword.length() < 59) {
                System.err.println("Invalid hash length: " + storedPassword.length() + " for user " + input);
                return null;
            }

            // jBCrypt chỉ hỗ trợ $2a$, nên chuyển $2y$ (PHP) và $2b$ sang $2a$
            String hashForCheck = storedPassword;
            if (storedPassword.startsWith("$2y$") || storedPassword.startsWith("$2b$")) {
                hashForCheck = "$2a$" + storedPassword.substring(4);
            }

            try {
                if (!BCrypt.checkpw(password, hashForCheck)) {
                    return null;
                }
            } catch (IllegalArgumentException e) {
                System.err.println("BCrypt error for user " + input + ": " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else {
            // Password cũ chưa hash (plain text) — so sánh trực tiếp rồi migrate sang BCrypt
            if (!password.equals(storedPassword)) {
                return null;
            }
            // Migrate: hash lại password và lưu vào DB
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            userDao.updatePassword(user.getId(), hashed);
            user.setPassword(hashed);
        }
        return user;
    }

    public User loginByProvider(String provider, String providerId) {
        return userDao.loginByProvider(provider, providerId);
    }

    // Đăng nhập qua provider (Facebook/Google...).
    // Flow:
    //   1) Đã từng đăng nhập bằng provider này (provider + provider_id khớp) -> đăng nhập luôn.
    //   2) Email từ provider đã tồn tại trong hệ thống -> đăng nhập vào account đó
    //      và liên kết provider để lần sau đăng nhập trực tiếp ở bước 1.
    //   3) Chưa có account -> tạo mới.
    public User loginOrRegisterSocial(User u) {
        // 1. Tài khoản đã liên kết provider trước đó
        User existed = userDao.loginByProvider(u.getProvider(), u.getProviderId());
        if (existed != null) {
            return existed;
        }

        // 2. Email tồn tại trong hệ thống -> đăng nhập vào account hiện có
        String email = u.getEmail();
        if (email != null && !email.isBlank()) {
            Optional<User> byEmailOpt = userDao.findByEmail(email);
            if (byEmailOpt.isPresent()) {
                User byEmail = byEmailOpt.get();
                // Tài khoản đã bị khóa thì không cho đăng nhập
                if (byEmail.getStatus() != 1) {
                    return null;
                }
                userDao.linkSocialProvider(byEmail.getId(), u.getProvider(), u.getProviderId(), u.getAvatar());
                User refreshed = userDao.loginByProvider(u.getProvider(), u.getProviderId());
                return refreshed != null ? refreshed : byEmail;
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
        userDao.insertSocialUser(u);
        return userDao.loginByProvider(u.getProvider(), u.getProviderId());
    }
}
