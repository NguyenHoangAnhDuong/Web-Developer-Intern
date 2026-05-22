package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;

import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.dao.UserDao;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.RedisService;

@WebServlet(name = "ResetPasswordServlet", value = "/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!hasValidResetToken(request)) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        request.getRequestDispatcher("/views/user/reset-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (!hasValidResetToken(request)) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String token    = (String) session.getAttribute("reset_token");
        String email    = RedisService.getResetTokenEmail(token);
        String password = nullToEmpty(request.getParameter("password"));
        String confirm  = nullToEmpty(request.getParameter("confirm"));

        if (email == null) {
            // Token đã hết hạn / bị thu hồi giữa GET và POST — yêu cầu nhập lại email.
            session.removeAttribute("reset_token");
            session.removeAttribute("reset_email");
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        if (password.isEmpty()) {
            forwardWithError(request, response, "Vui lòng nhập mật khẩu mới.");
            return;
        }
        if (!isStrongPassword(password)) {
            forwardWithError(request, response,
                    "Mật khẩu tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&).");
            return;
        }
        if (!password.equals(confirm)) {
            forwardWithError(request, response, "Mật khẩu xác nhận không khớp.");
            return;
        }

        User user = userDao.findByEmail(email).orElse(null);
        if (user == null) {
            // Cực hiếm — email đã bị xoá giữa các bước.
            RedisService.deleteResetToken(token);
            session.removeAttribute("reset_token");
            session.removeAttribute("reset_email");
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        boolean ok = userDao.updatePassword(user.getId(), hashed);
        if (!ok) {
            forwardWithError(request, response, "Lỗi hệ thống, vui lòng thử lại.");
            return;
        }

        // Dọn dẹp — chống dùng lại token, và đánh dấu để cho phép vào trang success.
        RedisService.deleteResetToken(token);
        session.removeAttribute("reset_token");
        session.removeAttribute("reset_email");
        session.setAttribute("reset_password_done", Boolean.TRUE);

        response.sendRedirect(request.getContextPath() + "/reset-password-success");
    }

    private boolean hasValidResetToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        String token = (String) session.getAttribute("reset_token");
        if (token == null) return false;
        return RedisService.getResetTokenEmail(token) != null;
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("/views/user/reset-password.jsp").forward(request, response);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
