package vn.edu.hcmuaf.fit.ttltw.service;

import org.mindrot.jbcrypt.BCrypt;
import vn.edu.hcmuaf.fit.ttltw.dao.UserDao;
import vn.edu.hcmuaf.fit.ttltw.model.User;

import java.util.List;
import java.util.regex.Pattern;

public class EmployeeService {
    private final UserDao userDao = UserDao.getInstance();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_.]{4,30}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-#^()])[A-Za-z\\d@$!%*?&_\\-#^()]{8,}$"
    );

    public static class ValidationError extends RuntimeException {
        public ValidationError(String m) { super(m); }
    }

    // Tạo nhân viên: chỉ cần họ tên (fullName), username, email, password
    public int createEmployee(String fullName, String username, String email, String rawPassword, int rolesId) {
        if (fullName == null || fullName.isBlank())
            throw new ValidationError("Họ tên không được trống");
        if (username == null || !USERNAME_PATTERN.matcher(username).matches())
            throw new ValidationError("Tên đăng nhập 4-30 ký tự (chữ, số, dấu _ .)");
        if (email == null || !EMAIL_PATTERN.matcher(email).matches())
            throw new ValidationError("Email không hợp lệ");
        if (rawPassword == null || !PASSWORD_PATTERN.matcher(rawPassword).matches())
            throw new ValidationError("Mật khẩu tối thiểu 8 ký tự, gồm hoa, thường, số, ký tự đặc biệt");
        if (userDao.checkExistUsername(username))
            throw new ValidationError("Tên đăng nhập đã tồn tại");
        if (userDao.checkExistEmail(email))
            throw new ValidationError("Email đã tồn tại");
        if (rolesId <= 0)
            throw new ValidationError("Vai trò không hợp lệ");

        // Tách fullName -> lastName (phần cuối) + firstName (còn lại)
        String fn = fullName.trim();
        String firstName = fn;
        String lastName = "";
        int sp = fn.lastIndexOf(' ');
        if (sp > 0) {
            firstName = fn.substring(0, sp).trim();
            lastName = fn.substring(sp + 1).trim();
        }

        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        return userDao.createEmployee(firstName, lastName, username.trim(), email.trim(), hashed, rolesId);
    }

    public List<User> listEmployees(String search, int offset, int limit) {
        return userDao.getEmployeesPaginated(search, offset, limit);
    }

    public int countEmployees(String search) {
        return userDao.countEmployees(search);
    }

    public boolean updateEmployee(int id, int rolesId, int status) {
        return userDao.updateEmployeeRoleStatus(id, rolesId, status);
    }
}
