package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.service.EmailService;
import vn.edu.hcmuaf.fit.ttltw.service.RedisService;

@WebServlet(name = "VerifyResetOtpServlet", value = "/verify-reset-otp")
public class VerifyResetOtpServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("reset_email") == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String email = (String) session.getAttribute("reset_email");
        request.setAttribute("maskedEmail", maskEmail(email));
        request.getRequestDispatcher("/views/user/verify-reset-otp.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("reset_email") == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String email  = (String) session.getAttribute("reset_email");
        String action = request.getParameter("action");

        // Gửi lại OTP
        if ("resend".equals(action)) {
            String newOtp = generateOtp();
            RedisService.saveResetOtp(email, newOtp);

            try {
                EmailService.sendResetOtp(email, newOtp);
                session.setAttribute("toastMessage", "Đã gửi lại mã OTP vào email của bạn!");
                session.setAttribute("toastType", "success");
            } catch (Exception e) {
                session.setAttribute("toastMessage", "Gửi email thất bại. Vui lòng thử lại!");
                session.setAttribute("toastType", "error");
            }

            response.sendRedirect(request.getContextPath() + "/verify-reset-otp");
            return;
        }

        // Xác thực OTP
        String inputOtp  = request.getParameter("otp");
        String storedOtp = RedisService.getResetOtp(email);

        if (storedOtp == null) {
            forwardWithError(request, response, email, "Mã OTP đã hết hạn. Nhấn \"Gửi lại\" để nhận mã mới!");
            return;
        }

        if (!storedOtp.equals(inputOtp)) {
            forwardWithError(request, response, email, "Mã OTP không đúng. Vui lòng kiểm tra lại!");
            return;
        }

        // OTP hợp lệ — cấp reset token (TTL 10 phút) cho bước nhập mật khẩu mới.
        RedisService.deleteResetOtp(email);
        String resetToken = UUID.randomUUID().toString();
        RedisService.saveResetToken(resetToken, email);
        session.setAttribute("reset_token", resetToken);

        response.sendRedirect(request.getContextPath() + "/reset-password");
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response,
                                  String email, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("maskedEmail", maskEmail(email));
        request.getRequestDispatcher("/views/user/verify-reset-otp.jsp").forward(request, response);
    }

    private String generateOtp() {
        int code = 100000 + new SecureRandom().nextInt(900000);
        return String.valueOf(code);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        String visible = email.substring(0, 2);
        String stars   = "*".repeat(atIndex - 2);
        return visible + stars + email.substring(atIndex);
    }
}
