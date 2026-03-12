package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;
import vn.edu.hcmuaf.fit.ttltw.utils.SidebarUtil;

import java.io.IOException;

@WebServlet("/user/change-password")
public class ChangepassServlet extends HttpServlet {

    private UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Set sidebar data
        req.setAttribute("activeMenu", "password");
        SidebarUtil.setSidebarData(req);

        req.getRequestDispatcher("form_change_pass.jsp");
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        // Kiểm tra nếu người dùng đăng nhập bằng Facebook
        if (currentUser != null && "facebook".equals(currentUser.getProvider())) {
            session.setAttribute("toastMessage", "Tài khoản đăng nhập bằng Facebook không có mật khẩu. Vui lòng đổi mật khẩu trên Facebook.");
            session.setAttribute("toastType", "error");
            resp.sendRedirect(req.getContextPath() + "/user/profile");
            return;
        }

        req.getRequestDispatcher("/views/user/form_change_pass.jsp")
                .forward(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            resp.sendRedirect("/login");
            return;
        }

        int userId = currentUser.getId();
        String password = req.getParameter("password");
        String newPass = req.getParameter("new_password");
        String confirm = req.getParameter("confirm_password");
        if (!newPass.equals(confirm)) {
            req.setAttribute("error", "Mật khẩu xác nhận không khớp!");
            req.getRequestDispatcher("/views/user/form_change_pass.jsp").forward(req, resp);
            return;
        }
        String result = userService.updatePassword(userId,password, newPass);
        if (result.equals("Đổi mật khẩu thành công")) {
            req.setAttribute("message", result);
        } else {
            req.setAttribute("error", result);
        }
        req.getRequestDispatcher("/views/user/form_change_pass.jsp").forward(req, resp);
        // Set sidebar data
        req.setAttribute("activeMenu", "password");
        SidebarUtil.setSidebarData(req);

        req.getRequestDispatcher("form_change_pass.jsp").forward(req, resp);
    }

}
