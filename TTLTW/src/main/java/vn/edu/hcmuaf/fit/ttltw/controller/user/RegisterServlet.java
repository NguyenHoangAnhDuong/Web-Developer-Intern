package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.dao.UserDao;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.CacheService;
import vn.edu.hcmuaf.fit.ttltw.service.EmailService;

@WebServlet(name = "RegisterServlet", value = "/register")
public class RegisterServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/user/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fname    = nullToEmpty(request.getParameter("fname")).trim();
        String lname    = nullToEmpty(request.getParameter("lname")).trim();
        String email    = nullToEmpty(request.getParameter("email")).trim();
        String username = nullToEmpty(request.getParameter("username")).trim();
        String password = nullToEmpty(request.getParameter("password"));
        String confirm  = nullToEmpty(request.getParameter("confirm"));
        boolean acceptTerms = "on".equalsIgnoreCase(request.getParameter("terms"))
                || "true".equalsIgnoreCase(request.getParameter("terms"));

        // Map lỗi theo từng field — JSP sẽ render inline dưới input tương ứng.
        Map<String, String> errors = new LinkedHashMap<>();

        if (fname.isEmpty()) {
            errors.put("fname", "Vui lòng nhập họ.");
        } else if (fname.length() > 50) {
            errors.put("fname", "Họ không được vượt quá 50 ký tự.");
        }

        if (lname.isEmpty()) {
            errors.put("lname", "Vui lòng nhập tên.");
        } else if (lname.length() > 50) {
            errors.put("lname", "Tên không được vượt quá 50 ký tự.");
        }

        if (email.isEmpty()) {
            errors.put("email", "Vui lòng nhập email.");
        } else if (!isValidEmail(email)) {
            errors.put("email", "Email không đúng định dạng.");
        }

        if (username.isEmpty()) {
            errors.put("username", "Vui lòng nhập tên đăng nhập.");
        } else if (username.length() < 4 || username.length() > 30) {
            errors.put("username", "Tên đăng nhập phải từ 4 đến 30 ký tự.");
        } else if (!isValidUsername(username)) {
            errors.put("username", "Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới.");
        }

        if (password.isEmpty()) {
            errors.put("password", "Vui lòng nhập mật khẩu.");
        } else if (!isStrongPassword(password)) {
            errors.put("password",
                    "Mật khẩu tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&).");
        }

        if (confirm.isEmpty()) {
            errors.put("confirm", "Vui lòng xác nhận mật khẩu.");
        } else if (!password.isEmpty() && !password.equals(confirm)) {
            errors.put("confirm", "Mật khẩu xác nhận không khớp.");
        }

        if (!acceptTerms) {
            errors.put("terms", "Bạn cần đồng ý với điều khoản dịch vụ để tiếp tục.");
        }

        // Chỉ truy DB khi các kiểm tra cú pháp đã pass — tránh query không cần thiết.
        if (!errors.containsKey("username") && userDao.checkExistUsername(username)) {
            errors.put("username", "Tên đăng nhập \"" + username + "\" đã được sử dụng.");
        }
        if (!errors.containsKey("email") && userDao.checkExistEmail(email)) {
            errors.put("email", "Email \"" + email + "\" đã được đăng ký.");
        }

        if (!errors.isEmpty()) {
            forwardWithErrors(request, response, errors,
                    fname, lname, email, username);
            return;
        }

        // Hash password trước khi lưu vào Redis
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Tạo đối tượng user chờ xác thực
        User pendingUser = new User(fname, lname, username, hashedPassword, email);

        // Lưu user vào Redis (TTL 10 phút)
        CacheService.savePendingUser(email, pendingUser);

        // Sinh và lưu OTP (TTL 5 phút)
        String otp = generateOtp();
        CacheService.saveOtp(email, otp);

        // Gửi OTP qua email
        try {
            EmailService.sendOtp(email, otp);
        } catch (Exception e) {
            CacheService.deletePendingUser(email);
            CacheService.deleteOtp(email);
            Map<String, String> mailErr = new HashMap<>();
            mailErr.put("general", "Không thể gửi email xác thực. Vui lòng thử lại.");
            forwardWithErrors(request, response, mailErr, fname, lname, email, username);
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("pending_email", email);
        response.sendRedirect(request.getContextPath() + "/verify-otp");
    }

    private void forwardWithErrors(HttpServletRequest request, HttpServletResponse response,
                                   Map<String, String> errors,
                                   String fname, String lname, String email, String username)
            throws ServletException, IOException {
        // Giữ lại giá trị đã nhập (trừ password) để user không phải gõ lại
        Map<String, String> form = new HashMap<>();
        form.put("fname", fname);
        form.put("lname", lname);
        form.put("email", email);
        form.put("username", username);

        request.setAttribute("errors", errors);
        request.setAttribute("form", form);
        request.getRequestDispatcher("/views/user/register.jsp").forward(request, response);
    }

    private String generateOtp() {
        int code = 100000 + new SecureRandom().nextInt(900000);
        return String.valueOf(code);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
