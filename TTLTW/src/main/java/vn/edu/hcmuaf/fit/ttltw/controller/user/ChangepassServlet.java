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
import java.util.regex.Pattern;

@WebServlet("/user/change-password")
public class ChangepassServlet extends HttpServlet {

    private UserService userService = new UserService();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-#^()])[A-Za-z\\d@$!%*?&_\\-#^()]{8,}$"
    );
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Set sidebar data
        req.setAttribute("activeMenu", "password");
        SidebarUtil.setSidebarData(req);

        HttpSession session = req.getSession();
        if ("true".equals(req.getParameter("success"))) {
            if (session != null) session.invalidate();
            req.setAttribute("passwordChangedSuccess", true);
            req.setAttribute("contextPath", req.getContextPath());
            req.getRequestDispatcher("/views/user/form_change_pass.jsp").forward(req, resp);
            return;
        }
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
        if (isBlank(password) || isBlank(newPass) || isBlank(confirm)) {
            session.setAttribute("toastMessage", "Vui lòng nhập đầy đủ thông tin!");
            session.setAttribute("toastType", "error");
            resp.sendRedirect(req.getContextPath() + "/user/change-password");
            return;
        }
        if (!PASSWORD_PATTERN.matcher(newPass).matches()) {
            session.setAttribute("toastMessage",
                    "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
            session.setAttribute("toastType", "error");
            resp.sendRedirect(req.getContextPath() + "/user/change-password");
            return;
        }
        if (!newPass.equals(confirm)) {
            session.setAttribute("toastMessage", "Mật khẩu xác nhận không khớp!");
            session.setAttribute("toastType", "error");
            resp.sendRedirect(req.getContextPath() + "/user/change-password");
            return;
        }
        String result = userService.updatePassword(userId,password, newPass);
        if (result.equals("Đổi mật khẩu thành công")) {
//            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/user/change-password?success=true");
        } else {
            session.setAttribute("toastMessage", result);
            session.setAttribute("toastType", "error");
            resp.sendRedirect(req.getContextPath() + "/user/change-password");
        }
    }
    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
