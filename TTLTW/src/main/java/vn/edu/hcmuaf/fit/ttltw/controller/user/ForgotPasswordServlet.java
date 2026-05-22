package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;
import java.security.SecureRandom;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.dao.UserDao;
import vn.edu.hcmuaf.fit.ttltw.service.EmailService;
import vn.edu.hcmuaf.fit.ttltw.service.RedisService;

@WebServlet(name = "ForgotPasswordServlet", value = "/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/user/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String email = nullToEmpty(request.getParameter("email")).trim();

        if (email.isEmpty()) {
            forwardWithError(request, response, email, "Vui lòng nhập email.");
            return;
        }
        if (!isValidEmail(email)) {
            forwardWithError(request, response, email, "Email không đúng định dạng.");
            return;
        }

        // Yêu cầu nghiệp vụ: chỉ gửi OTP khi email tồn tại trong hệ thống.
        if (!userDao.checkExistEmail(email)) {
            forwardWithError(request, response, email,
                    "Email \"" + email + "\" chưa được đăng ký trong hệ thống.");
            return;
        }

        String otp = generateOtp();
        RedisService.saveResetOtp(email, otp);

        try {
            EmailService.sendResetOtp(email, otp);
        } catch (Exception e) {
            RedisService.deleteResetOtp(email);
            forwardWithError(request, response, email,
                    "Không thể gửi email xác thực. Vui lòng thử lại.");
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("reset_email", email);
        // Đánh dấu OTP chưa verify để các bước sau (reset-password) không bị truy cập trực tiếp.
        session.removeAttribute("reset_token");
        response.sendRedirect(request.getContextPath() + "/verify-reset-otp");
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response,
                                  String email, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("emailValue", email);
        request.getRequestDispatcher("/views/user/forgot-password.jsp").forward(request, response);
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
}
