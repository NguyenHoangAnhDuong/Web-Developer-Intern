package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;
import vn.edu.hcmuaf.fit.ttltw.utils.SidebarUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "UserProfileServlet", urlPatterns = { "/user/profile" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class InfoUserServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // load fresh user
        User sessionUser = (User) session.getAttribute("user");
        User freshUser = userService.getUserProfileById(sessionUser.getId()).orElse(sessionUser);

        session.setAttribute("user", freshUser);
        req.setAttribute("user", freshUser);

        // Set sidebar data
        req.setAttribute("activeMenu", "profile");
        SidebarUtil.setSidebarData(req);

        req.getRequestDispatcher("/views/user/info-user.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.getWriter().write("{\"success\": false, \"message\": \"Bạn chưa đăng nhập\"}");
            return;
        }

        User user = (User) session.getAttribute("user");
        String action = req.getParameter("action");
        if (action == null)
            action = "";

        switch (action) {
            case "update":
                updateInfo(req, resp, session, user);
                break;

            case "upload-avatar":
                uploadAvatar(req, resp, session, user);
                break;

            default:
                resp.getWriter().write("{\"success\": false, \"message\": \"Hành động không hợp lệ\"}");
        }
    }
    // UPDATE THÔNG TIN
    private void updateInfo(HttpServletRequest req, HttpServletResponse resp,
            HttpSession session, User user)
            throws IOException {

        PrintWriter out = resp.getWriter();

        String first = safe(req.getParameter("firstName"));
        String last = safe(req.getParameter("lastName"));
        String email = safe(req.getParameter("email"));

        if (email.isEmpty()) {
            out.write("{\"success\": false, \"message\": \"Email không được để trống\"}");
            return;
        }

        if (!email.equals(user.getEmail())
                && userService.checkExistEmailForOtherUsers(user.getId(), email)) {

            out.write("{\"success\": false, \"message\": \"Email đã tồn tại\"}");
            return;
        }

        boolean ok = userService.updateUserInfo(user.getId(), first, last, email);

        if (ok) {
            User updated = userService.getUserProfileById(user.getId()).orElse(user);
            session.setAttribute("user", updated);
            out.write("{\"success\": true}");
        } else {
            out.write("{\"success\": false, \"message\": \"Cập nhật thất bại\"}");
        }
    }

    // UPLOAD AVATAR
    private void uploadAvatar(HttpServletRequest req, HttpServletResponse resp,
            HttpSession session, User user)
            throws IOException, ServletException {

        PrintWriter out = resp.getWriter();

        Part filePart = req.getPart("avatar");

        if (filePart == null || filePart.getSize() == 0) {
            out.write("{\"success\": false, \"message\": \"Không có file\"}");
            return;
        }

        String submitted = filePart.getSubmittedFileName();
        String extension = getExtension(submitted);
        String fileName = "avatar_" + user.getId() + extension;

        String uploadDir = req.getServletContext().getRealPath("/uploads/avatars");

        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();

        String absolutePath = uploadDir + File.separator + fileName;

        filePart.write(absolutePath);

        String avatarUrl = "/uploads/avatars/" + fileName;

        boolean updated = userService.updateAvatar(user.getId(), avatarUrl);

        if (updated) {
            User updatedUser = userService.getUserProfileById(user.getId()).orElse(user);
            session.setAttribute("user", updatedUser);

            out.write("{\"success\": true, \"url\": \"" + req.getContextPath() + avatarUrl + "\"}");
        } else {
            out.write("{\"success\": false, \"message\": \"Lưu avatar thất bại\"}");
        }
    }

    // UTILITIES
    private String getExtension(String name) {
        if (name == null || !name.contains("."))
            return ".png";
        String ext = name.substring(name.lastIndexOf(".")).toLowerCase();
        if (!ext.matches("\\.(png|jpg|jpeg|gif)"))
            return ".png";
        return ext;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
