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

    // Đăng nhập hoặc đăng ký qua mạng xã hội (Facebook)
    public User loginOrRegisterSocial(User u, String provider, String providerUserId, String providerEmail) {
        // 1. Tìm theo provider + providerUserId trong user_social_accounts
        User existed = userDao.findByProvider(provider, providerUserId);
        if (existed != null) {
            return existed;
        }

        // 2. Chưa có → tạo user mới + liên kết social account
        int userId = userDao.insertSocialUser(u);
        userDao.linkSocialAccount(userId, provider, providerUserId, providerEmail);
        return userDao.findById(userId).orElse(null);
    }

    // Đăng nhập bằng Google: kiểm tra email đã tồn tại trong hệ thống chưa
    // Nếu có → liên kết Google account; nếu chưa → tạo mới
    public User loginOrRegisterByGoogle(User googleUser, String googleId, String email) {
        // 1. Tìm theo provider + googleId (đã đăng nhập Google trước đó)
        User existed = userDao.findByProvider("google", googleId);
        if (existed != null) {
            return existed;
        }

        // 2. Tìm theo email đã có trong hệ thống → liên kết Google account
        existed = userDao.findByEmail(email);
        if (existed != null) {
            userDao.linkSocialAccount(existed.getId(), "google", googleId, email);
            return existed;
        }

        // 3. Chưa có → tạo tài khoản mới + liên kết
        int userId = userDao.insertSocialUser(googleUser);
        userDao.linkSocialAccount(userId, "google", googleId, email);
        return userDao.findById(userId).orElse(null);
    }
}
