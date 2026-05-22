package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ResetPasswordSuccessServlet", value = "/reset-password-success")
public class ResetPasswordSuccessServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Object flag = (session == null) ? null : session.getAttribute("reset_password_done");
        if (flag == null) {
            // Tránh user truy cập trực tiếp /reset-password-success mà không qua luồng quên mật khẩu.
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // One-shot — refresh sẽ bị đẩy về /login.
        session.removeAttribute("reset_password_done");
        request.getRequestDispatcher("/views/user/reset-password-success.jsp").forward(request, response);
    }
}
