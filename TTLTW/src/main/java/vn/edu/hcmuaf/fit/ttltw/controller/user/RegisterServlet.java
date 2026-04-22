package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;
import java.security.SecureRandom;

import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.dao.UserDao;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.EmailService;
import vn.edu.hcmuaf.fit.ttltw.service.RedisService;

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

        String fname    = request.getParameter("fname").trim();
        String lname    = request.getParameter("lname").trim();
        String email    = request.getParameter("email").trim();
        String username = request.getParameter("username").trim();
        String password = request.getParameter("password");
        String confirm  = request.getParameter("confirm");

        // Kiểm tra rỗng
        if (fname.isEmpty() || lname.isEmpty() || email.isEmpty()
                || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            forwardWithError(request, response, "Vui lòng điền đầy đủ tất cả các trường!");
            return;
        }

        // Kiểm tra định dạng email
        if (!isValidEmail(email)) {
            forwardWithError(request, response, "Email không hợp lệ!");
            return;
        }

        // Kiểm tra username
        if (!isValidUsername(username)) {
            forwardWithError(request, response, "Username chỉ được chứa chữ cái, số và dấu gạch dưới!");
            return;
        }

        // Kiểm tra mật khẩu khớp
        if (!password.equals(confirm)) {
            forwardWithError(request, response, "Mật khẩu xác nhận không khớp!");
            return;
        }

        // Kiểm tra độ mạnh mật khẩu
        if (!isStrongPassword(password)) {
            forwardWithError(request, response, "Mật khẩu phải ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
            return;
        }

        // Kiểm tra username đã tồn tại chưa
        if (userDao.checkExistUsername(username)) {
            forwardWithError(request, response, "Tên đăng nhập \"" + username + "\" đã được sử dụng!");
            return;
        }

        // Kiểm tra email đã tồn tại chưa
        if (userDao.checkExistEmail(email)) {
            forwardWithError(request, response, "Email \"" + email + "\" đã được đăng ký!");
            return;
        }

        // Hash password trước khi lưu vào Redis
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Tạo đối tượng user chờ xác thực
        User pendingUser = new User(fname, lname, username, hashedPassword, email);

        // Lưu user vào Redis (TTL 10 phút)
        RedisService.savePendingUser(email, pendingUser);

        // Sinh và lưu OTP (TTL 5 phút)
        String otp = generateOtp();
        RedisService.saveOtp(email, otp);

        // Gửi OTP qua email
        try {
            EmailService.sendOtp(email, otp);
        } catch (Exception e) {
            // Dọn dẹp Redis nếu gửi mail thất bại
            RedisService.deletePendingUser(email);
            RedisService.deleteOtp(email);
            forwardWithError(request, response, "Không thể gửi email xác thực. Vui lòng thử lại!");
            return;
        }

        // Lưu email vào session để VerifyOtpServlet sử dụng
        HttpSession session = request.getSession();
        session.setAttribute("pending_email", email);

        response.sendRedirect(request.getContextPath() + "/verify-otp");
    }


    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("/views/user/register.jsp").forward(request, response);
    }

    private String generateOtp() {
        int code = 100000 + new SecureRandom().nextInt(900000);
        return String.valueOf(code);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
