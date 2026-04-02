package vn.edu.hcmuaf.fit.ttltw.controller.user;

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

import java.io.IOException;
import java.security.SecureRandom;

@WebServlet(name = "VerifyOtpServlet", value = "/verify-otp")
public class VerifyOtpServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("pending_email") == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        String email = (String) session.getAttribute("pending_email");
        request.setAttribute("maskedEmail", maskEmail(email));
        request.getRequestDispatcher("/views/user/verify-otp.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("pending_email") == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        String email  = (String) session.getAttribute("pending_email");
        String action = request.getParameter("action");

        // ── Gửi lại OTP ─────────────────────────────────────────────────────────
        if ("resend".equals(action)) {
            User pendingUser = RedisService.getPendingUser(email);
            if (pendingUser == null) {
                session.removeAttribute("pending_email");
                session.setAttribute("toastMessage", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại!");
                session.setAttribute("toastType", "error");
                response.sendRedirect(request.getContextPath() + "/register");
                return;
            }

            String newOtp = generateOtp();
            RedisService.saveOtp(email, newOtp);

            try {
                EmailService.sendOtp(email, newOtp);
                session.setAttribute("toastMessage", "Đã gửi lại mã OTP vào email của bạn!");
                session.setAttribute("toastType", "success");
            } catch (Exception e) {
                session.setAttribute("toastMessage", "Gửi email thất bại. Vui lòng thử lại!");
                session.setAttribute("toastType", "error");
            }

            response.sendRedirect(request.getContextPath() + "/verify-otp");
            return;
        }

        // ── Xác thực OTP ────────────────────────────────────────────────────────
        String inputOtp  = request.getParameter("otp");
        String storedOtp = RedisService.getOtp(email);

        if (storedOtp == null) {
            forwardWithError(request, response, email, "Mã OTP đã hết hạn. Nhấn \"Gửi lại\" để nhận mã mới!");
            return;
        }

        if (!storedOtp.equals(inputOtp)) {
            forwardWithError(request, response, email, "Mã OTP không đúng. Vui lòng kiểm tra lại!");
            return;
        }

        // OTP hợp lệ — lấy thông tin user từ Redis
        User pendingUser = RedisService.getPendingUser(email);
        if (pendingUser == null) {
            forwardWithError(request, response, email, "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại!");
            return;
        }

        // Tạo tài khoản trong DB (password đã được hash từ RegisterServlet)
        boolean success = userDao.register(pendingUser);
        if (!success) {
            forwardWithError(request, response, email, "Lỗi hệ thống, vui lòng thử lại!");
            return;
        }

        // Dọn dẹp Redis và session tạm
        RedisService.deleteOtp(email);
        RedisService.deletePendingUser(email);
        session.removeAttribute("pending_email");

        // Lấy user đầy đủ (có id, rolesId) để set session đăng nhập
        User registeredUser = userDao.findByInput(email);
        if (registeredUser != null) {
            session.setAttribute("user", registeredUser);
            session.setAttribute("role", registeredUser.getRolesId());
            session.setAttribute("toastMessage", "Đăng ký thành công. Chào mừng bạn!");
            session.setAttribute("toastType", "success");
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            response.sendRedirect(request.getContextPath() + "/login?message=register_success");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response,
                                  String email, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("maskedEmail", maskEmail(email));
        request.getRequestDispatcher("/views/user/verify-otp.jsp").forward(request, response);
    }

    private String generateOtp() {
        int code = 100000 + new SecureRandom().nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Che một phần email: "testuser@gmail.com" → "te******@gmail.com"
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        String visible = email.substring(0, 2);
        String stars   = "*".repeat(atIndex - 2);
        return visible + stars + email.substring(atIndex);
    }
}
