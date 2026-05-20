package vn.edu.hcmuaf.fit.ttltw.dao;

import org.mindrot.jbcrypt.BCrypt;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.User;

import java.util.List;
import java.util.Optional;

public class UserDao {

    private static UserDao instance;

    public static UserDao getInstance() {
        if (instance == null)
            instance = new UserDao();
        return instance;
    }

    // Kiểm tra đã có username tồn tại chưa
    public boolean checkExistUsername(String username) {
        Integer count = DBConnect.getJdbi()
                .withHandle(h -> h.createQuery("SELECT COUNT(*) FROM users WHERE username = :u")
                        .bind("u", username)
                        .mapTo(Integer.class)
                        .one());
        return count > 0;
    }

    // Kiểm tra đã có email tồn tại chưa
    public boolean checkExistEmail(String email) {
        Integer count = DBConnect.getJdbi()
                .withHandle(h -> h.createQuery("SELECT COUNT(*) FROM users WHERE email = :e")
                        .bind("e", email)
                        .mapTo(Integer.class)
                        .one());
        return count > 0;
    }

    // Kiểm tra email đã tồn tại cho user khác chưa
    public boolean checkExistEmailForOtherUsers(int id, String email) {
        Integer count = DBConnect.getJdbi()
                .withHandle(h -> h.createQuery("SELECT COUNT(*) FROM users WHERE email = :e AND id != :id")
                        .bind("e", email)
                        .bind("id", id)
                        .mapTo(Integer.class)
                        .one());
        return count > 0;
    }

    public boolean register(User u) {
        int result = DBConnect.getJdbi()
                .withHandle(h -> h
                        .createUpdate("INSERT INTO users (first_name, last_name, username, password, email, roles_id) " +
                                "VALUES (:fn, :ln, :un, :pw, :em, 2)")
                        .bind("fn", u.getFirstName())
                        .bind("ln", u.getLastName())
                        .bind("un", u.getUsername())
                        .bind("pw", u.getPassword())
                        .bind("em", u.getEmail())
                        .execute());
        return result > 0;
    }

    public User login(String input, String password) {
        String sql = """
                SELECT id, username, first_name AS firstName, last_name AS lastName, avatar, email,
                       roles_id AS rolesId, status, created_at AS createdAt, updated_at AS updatedAt
                FROM users
                WHERE (email = ? OR username = ?)
                  AND password = ?
                  AND status = 1
                LIMIT 1
                """;

        return DBConnect.getJdbi().withHandle(handle -> handle.createQuery(sql)
                .bind(0, input)
                .bind(1, input)
                .bind(2, password)
                .mapToBean(User.class)
                .findOne()
                .orElse(null));
    }

    // Lấy thông tin đầy đủ khách hàng theo id (chỉ lấy role = 2)
    public Optional<User> findCustomerDetailById(int id) {
        return DBConnect.getJdbi().withHandle(handle -> handle
                .createQuery("SELECT id, username, first_name AS firstName, last_name AS lastName, email, avatar, roles_id AS rolesId, status FROM users WHERE id = :id AND roles_id = 2")
                .bind("id", id)
                .mapToBean(User.class)
                .findOne());
    }

    // lây thông tin qua tìm id người dùng
    public Optional<User> findById(int id) {
        return DBConnect.getJdbi().withHandle(handle -> handle
                .createQuery("SELECT id, username, first_name AS firstName, last_name AS lastName, email, avatar FROM users WHERE id = :id")
                .bind("id", id)
                .mapToBean(User.class)
                .findOne());
    }

    // lấy thông tin qua tìm tên đăng nhập
    public Optional<User> findByUsername(String username) {
        return DBConnect.getJdbi().withHandle(handle -> handle.createQuery(
                "SELECT id, username, first_name AS firstName, last_name AS lastName, email, avatar FROM users WHERE username = :username")
                .bind("username", username)
                .mapToBean(User.class)
                .findOne());
    }

    public int countUsers(String searchTerm, String statusFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE roles_id = 2");

        // Filter theo search
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(
                    " AND (CAST(id AS CHAR) LIKE :search OR username LIKE :search OR email LIKE :search OR first_name LIKE :search OR last_name LIKE :search)");
        }

        // Filter theo status
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append(" AND status = :status");
        }

        return DBConnect.getJdbi().withHandle(handle -> {
            var query = handle.createQuery(sql.toString());
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                query.bind("search", "%" + searchTerm.trim() + "%");
            }
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                int status = "Hoạt động".equalsIgnoreCase(statusFilter) ? 1 : 0;
                query.bind("status", status);
            }
            return query.mapTo(Integer.class).one();
        });
    }

    public List<User> getUsersPaginated(String searchTerm, String statusFilter, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                    SELECT id, username, first_name AS firstName, last_name AS lastName, avatar, email, roles_id AS rolesId, status
                    FROM users
                    WHERE roles_id = 2
                """);
        // Filter theo search
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(
                    " AND (CAST(id AS CHAR) LIKE :search OR username LIKE :search OR email LIKE :search OR first_name LIKE :search OR last_name LIKE :search)");
        }
        // Filter theo status
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append(" AND status = :status");
        }
        // Sắp xếp và phân trang
        sql.append(" ORDER BY id DESC LIMIT :limit OFFSET :offset");
        return DBConnect.getJdbi().withHandle(handle -> {
            var query = handle.createQuery(sql.toString())
                    .bind("limit", limit)
                    .bind("offset", offset);
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                query.bind("search", "%" + searchTerm.trim() + "%");
            }
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                int status = "Hoạt động".equalsIgnoreCase(statusFilter) ? 1 : 0;
                query.bind("status", status);
            }
            return query.mapToBean(User.class).list();
        });
    }

    // Lấy danh sachs người dùng load từ database
    public List<User> getAllUsers() {
        String sql = """
                    SELECT id, username, first_name AS firstName, last_name AS lastName, avatar, email, roles_id AS rolesId, status
                    FROM users
                """;
        return DBConnect.getJdbi().withHandle(handle -> handle.createQuery(sql)
                .mapToBean(User.class)
                .list());
    }

    // Cập nhật người dùng bên admin
    public boolean updateUser(int id, int role, int status) {
        String sql = """
                    UPDATE users
                    SET roles_id = :rolesId, status = :status
                    WHERE id = :id
                """;
        int rows = DBConnect.getJdbi().withHandle(handle -> handle.createUpdate(sql)
                .bind("rolesId", role)
                .bind("status", status)
                .bind("id", id)
                .execute());

        return rows > 0;
    }

    // chỉnh sửa thông tin bên người dùng
    public boolean updateUserInfo(int id, String firstName, String lastName, String email) {
        String sql = """
                    UPDATE users SET first_name = :fn, last_name = :ln, email = :em
                    WHERE id = :id
                """;

        int rows = DBConnect.getJdbi().withHandle(h -> h.createUpdate(sql)
                .bind("fn", firstName)
                .bind("ln", lastName)
                .bind("em", email)
                .bind("id", id)
                .execute());
        return rows > 0;
    }

    public boolean updateAvatar(int id, String avatarUrl) {
        String sql = "UPDATE users SET avatar = :av WHERE id = :id";

        int rows = DBConnect.getJdbi().withHandle(h -> h.createUpdate(sql)
                .bind("av", avatarUrl)
                .bind("id", id)
                .execute());
        return rows > 0;
    }

    // Cập nhật mật khẩu
    public boolean updatePassword(int userId, String newPassword) {
        String sql = """
                UPDATE users
                SET password = :pw, updated_at = NOW()
                WHERE id = :id
                """;

        return DBConnect.getJdbi().withHandle(h -> h.createUpdate(sql)
                .bind("pw", newPassword) // hashed password
                .bind("id", userId)
                .execute() > 0);
    }

    public User findByInput(String input) {
        // Kiểm tra xem input có phải là email hay không
        boolean isEmail = input != null && input.contains("@");
        String sql;
        if (isEmail) {
            sql = """
                        SELECT id, username, first_name AS firstName, last_name AS lastName, avatar, email,
                               roles_id AS rolesId, status, password,
                               failed_login_attempts AS failedLoginAttempts,
                               locked_until AS lockedUntil,
                               created_at AS createdAt, updated_at AS updatedAt
                        FROM users
                        WHERE email = :input
                        LIMIT 1
                    """;
        } else {
            sql = """
                        SELECT id, username, first_name AS firstName, last_name AS lastName, avatar, email,
                               roles_id AS rolesId, status, password,
                               failed_login_attempts AS failedLoginAttempts,
                               locked_until AS lockedUntil,
                               created_at AS createdAt, updated_at AS updatedAt
                        FROM users
                        WHERE username = :input
                        LIMIT 1
                    """;
        }

        return DBConnect.getJdbi().withHandle(handle -> handle.createQuery(sql)
                .bind("input", input)
                .mapToBean(User.class)
                .findOne()
                .orElse(null));
    }

    // Tìm user theo email (dùng cho luồng đăng nhập qua provider khi email đã tồn tại trong hệ thống).
    // Không filter status — caller tự kiểm tra để hiển thị message phù hợp khi tài khoản bị khóa.
    public Optional<User> findByEmail(String email) {
        String sql = """
                    SELECT id, username, first_name AS firstName, last_name AS lastName, avatar, email,
                           roles_id AS rolesId, status,
                           failed_login_attempts AS failedLoginAttempts,
                           locked_until AS lockedUntil,
                           created_at AS createdAt, updated_at AS updatedAt
                    FROM users
                    WHERE email = :email
                    LIMIT 1
                """;
        return DBConnect.getJdbi().withHandle(handle -> handle.createQuery(sql)
                .bind("email", email)
                .mapToBean(User.class)
                .findOne());
    }

    // Set avatar cho user nếu user chưa có avatar (dùng khi liên kết provider lần đầu
    // để mượn ảnh đại diện từ FB/Google mà không ghi đè avatar người dùng đã tự upload).
    public boolean updateAvatarIfEmpty(int userId, String avatar) {
        if (avatar == null || avatar.isBlank()) return false;
        String sql = """
                    UPDATE users
                    SET avatar = :av,
                        updated_at = NOW()
                    WHERE id = :id AND (avatar IS NULL OR avatar = '')
                """;
        return DBConnect.getJdbi().withHandle(h -> h.createUpdate(sql)
                .bind("av", avatar)
                .bind("id", userId)
                .execute()) > 0;
    }

    // Đăng nhập qua provider — tra cứu trong bảng user_social_accounts để lấy user gốc.
    // KHÔNG filter status / lock — caller (service) tự inspect và quyết định message phù hợp.
    // Giữ consistency với findByEmail và tránh fallback ngoài ý muốn sang nhánh "tạo user mới"
    // khi user đã linked nhưng đang bị disable.
    public User loginByProvider(String provider, String providerUserId) {
        String sql = """
                SELECT u.id, u.username, u.first_name AS firstName, u.last_name AS lastName,
                       u.avatar, u.email, u.roles_id AS rolesId, u.status,
                       u.failed_login_attempts AS failedLoginAttempts,
                       u.locked_until AS lockedUntil,
                       u.created_at AS createdAt, u.updated_at AS updatedAt
                FROM user_social_accounts sa
                JOIN users u ON u.id = sa.user_id
                WHERE sa.provider = :provider AND sa.provider_user_id = :providerUserId
                LIMIT 1
                """;
        return DBConnect.getJdbi().withHandle(handle -> handle.createQuery(sql)
                .bind("provider", provider)
                .bind("providerUserId", providerUserId)
                .mapToBean(User.class)
                .findOne()
                .orElse(null));
    }

    // Tạo user mới cho đăng nhập bên thứ 3 (trả về user id)
    public int insertSocialUser(User u) {
        String sql = """
                INSERT INTO users (username, email, first_name, avatar, roles_id, status)
                VALUES (:un, :em, :fn, :av, :rolesId, :status)
                """;
        return DBConnect.getJdbi().withHandle(h -> h.createUpdate(sql)
                .bind("un", u.getUsername())
                .bind("em", u.getEmail())
                .bind("fn", u.getFirstName())
                .bind("av", u.getAvatar())
                .bind("rolesId", u.getRolesId())
                .bind("status", u.getStatus())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one());
    }

    // Liên kết tài khoản mạng xã hội với user
    public void linkSocialAccount(int userId, String provider, String providerUserId, String providerEmail) {
        String sql = """
                INSERT INTO user_social_accounts (user_id, provider, provider_user_id, provider_email)
                VALUES (:userId, :provider, :providerUserId, :providerEmail)
                ON DUPLICATE KEY UPDATE provider_email = :providerEmail
                """;
        DBConnect.getJdbi().withHandle(h -> h.createUpdate(sql)
                .bind("userId", userId)
                .bind("provider", provider)
                .bind("providerUserId", providerUserId)
                .bind("providerEmail", providerEmail)
                .execute());
    }

    // =========================================================
    // Employee management (dành cho module RBAC)
    // Nhân viên = user có role khác 'customer' và 'super_admin'
    // =========================================================

    public int createEmployee(String firstName, String lastName, String username, String email,
                              String hashedPassword, int rolesId) {
        String sql = """
                INSERT INTO users (first_name, last_name, username, password, email, roles_id, status)
                VALUES (:fn, :ln, :un, :pw, :em, :rid, 1)
                """;
        return DBConnect.getJdbi().withHandle(h -> h.createUpdate(sql)
                .bind("fn", firstName)
                .bind("ln", lastName)
                .bind("un", username)
                .bind("pw", hashedPassword)
                .bind("em", email)
                .bind("rid", rolesId)
                .executeAndReturnGeneratedKeys("id").mapTo(Integer.class).one());
    }

    public int countEmployees(String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM users u JOIN roles r ON r.id = u.roles_id " +
                        "WHERE r.name NOT IN ('customer','super_admin')");
        if (search != null && !search.isBlank()) {
            sql.append(" AND (u.username LIKE :s OR u.email LIKE :s " +
                    "OR u.first_name LIKE :s OR u.last_name LIKE :s)");
        }
        return DBConnect.getJdbi().withHandle(h -> {
            var q = h.createQuery(sql.toString());
            if (search != null && !search.isBlank()) q.bind("s", "%" + search.trim() + "%");
            return q.mapTo(Integer.class).one();
        });
    }

    public List<User> getEmployeesPaginated(String search, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT u.id, u.username, u.first_name AS firstName, u.last_name AS lastName,
                       u.email, u.roles_id AS rolesId, u.status
                FROM users u
                JOIN roles r ON r.id = u.roles_id
                WHERE r.name NOT IN ('customer','super_admin')
                """);
        if (search != null && !search.isBlank()) {
            sql.append(" AND (u.username LIKE :s OR u.email LIKE :s " +
                    "OR u.first_name LIKE :s OR u.last_name LIKE :s)");
        }
        sql.append(" ORDER BY u.id DESC LIMIT :lim OFFSET :off");
        return DBConnect.getJdbi().withHandle(h -> {
            var q = h.createQuery(sql.toString()).bind("lim", limit).bind("off", offset);
            if (search != null && !search.isBlank()) q.bind("s", "%" + search.trim() + "%");
            return q.mapToBean(User.class).list();
        });
    }

    public boolean updateEmployeeRoleStatus(int id, int rolesId, int status) {
        String sql = "UPDATE users SET roles_id = :rid, status = :st WHERE id = :id";
        return DBConnect.getJdbi().withHandle(h -> h.createUpdate(sql)
                .bind("rid", rolesId).bind("st", status).bind("id", id).execute()) > 0;
    }

    // =========================================================
    // Account lockout — đếm số lần đăng nhập sai và khóa tạm thời
    // =========================================================

    // Tăng failed_login_attempts thêm 1. Nếu sau khi tăng đạt ngưỡng thì set locked_until = NOW() + lockMinutes.
    // Trả về tổng số lần sai sau khi tăng (để caller biết còn bao nhiêu lần trước khi bị khóa).
    //
    // Bước 1 xóa lock + zero counter nếu lock cũ vừa hết hạn — nếu không, lần sai đầu tiên sau
    // chu kỳ khóa sẽ tăng counter (đang là maxAttempts) lên maxAttempts+1, vượt ngưỡng và bị
    // khóa lại ngay lập tức. Hai statement chạy trong cùng transaction để đảm bảo nguyên tử.
    public int incrementFailedAttempts(int userId, int maxAttempts, int lockMinutes) {
        return DBConnect.getJdbi().inTransaction(handle -> {
            handle.createUpdate("""
                            UPDATE users
                            SET failed_login_attempts = 0,
                                locked_until = NULL
                            WHERE id = :id
                              AND locked_until IS NOT NULL
                              AND locked_until <= NOW()
                            """)
                    .bind("id", userId)
                    .execute();

            handle.createUpdate("""
                            UPDATE users
                            SET failed_login_attempts = failed_login_attempts + 1,
                                locked_until = CASE
                                    WHEN failed_login_attempts + 1 >= :maxAttempts
                                        THEN DATE_ADD(NOW(), INTERVAL :lockMinutes MINUTE)
                                    ELSE locked_until
                                END
                            WHERE id = :id
                            """)
                    .bind("id", userId)
                    .bind("maxAttempts", maxAttempts)
                    .bind("lockMinutes", lockMinutes)
                    .execute();

            return handle.createQuery("SELECT failed_login_attempts FROM users WHERE id = :id")
                    .bind("id", userId)
                    .mapTo(Integer.class)
                    .findOne()
                    .orElse(0);
        });
    }

    // Reset counter + bỏ khóa khi đăng nhập thành công.
    public void resetFailedAttempts(int userId) {
        DBConnect.getJdbi().useHandle(h -> h.createUpdate("""
                        UPDATE users
                        SET failed_login_attempts = 0, locked_until = NULL
                        WHERE id = :id
                        """)
                .bind("id", userId)
                .execute());
    }

    public boolean checkPassword(int userId, String oldPass) {
        String sql = "SELECT password FROM users WHERE id = ?";

        String hashed = DBConnect.getJdbi().withHandle(handle -> handle.createQuery(sql)
                .bind(0, userId)
                .mapTo(String.class)
                .findOne()
                .orElse(null));

        if (hashed == null)
            return false;

        return BCrypt.checkpw(oldPass, hashed);
    }

}
