package vn.edu.hcmuaf.fit.ttltw.service;

import org.mindrot.jbcrypt.BCrypt;
import vn.edu.hcmuaf.fit.ttltw.dao.UserDao;
import vn.edu.hcmuaf.fit.ttltw.model.User;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    public Optional<User> getUserProfileById(int id) {
        return userDao.findById(id);
    }

    public Optional<User> getUserProfileByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    /**
     * Đếm tổng số user (có filter)
     */
    public int countUsers(String searchTerm, String roleFilter, String statusFilter) {
        return userDao.countUsers(searchTerm, roleFilter, statusFilter);
    }

    /**
     * Lấy danh sách user có phân trang (có filter)
     */
    public List<User> getUsersPaginated(String searchTerm, String roleFilter, String statusFilter, int offset,
            int limit) {
        return userDao.getUsersPaginated(searchTerm, roleFilter, statusFilter, offset, limit);
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

    // Đăng nhập (check BCrypt)
    public User login(String input, String password) {
        User user = userDao.findByInput(input);
        if (user == null)
            return null;

        if (!BCrypt.checkpw(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public User loginByProvider(String provider, String providerId) {
        return userDao.loginByProvider(provider, providerId);
    }

    // Kiểm tra đã tồn tại user chưa
    // Chưa thì thêm user mới
    public User loginOrRegisterSocial(User u) {
        User existed = userDao.loginByProvider(u.getProvider(), u.getProviderId());
        if (existed != null) {
            return existed;
        }

        userDao.insertSocialUser(u);
        return userDao.loginByProvider(u.getProvider(), u.getProviderId());
    }
}
