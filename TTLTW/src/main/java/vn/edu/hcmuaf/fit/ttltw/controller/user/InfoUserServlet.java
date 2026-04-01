package vn.edu.hcmuaf.fit.ttltw.controller.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;
import vn.edu.hcmuaf.fit.ttltw.utils.CloudinaryUtil;
import vn.edu.hcmuaf.fit.ttltw.utils.SidebarUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@WebServlet(name = "UserProfileServlet", urlPatterns = { "/user/profile" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class InfoUserServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }
    private static final Set<String> ALLOWED_TYPES = java.util.Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/gif"
    );
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
        String avatarPath = freshUser.getAvatar();
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            avatarPath = req.getContextPath() + "/asset/img/admin.jpg";
        }
        req.setAttribute("avatarPath", avatarPath);

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
        String email = user.getEmail();

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
        String contentType = filePart.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            out.write("{\"success\": false, \"message\": \"Chỉ chấp nhận file ảnh (PNG, JPG, GIF)\"}");
            return;
        }
        try {
            Cloudinary cloudinary = CloudinaryUtil.getInstance();
            byte[] fileBytes = filePart.getInputStream().readAllBytes();
            if (!isValidImageSignature(fileBytes)) {
                out.write("{\"success\": false, \"message\": \"File không hợp lệ\"}");
                return;
            }
            var upload = cloudinary.uploader().upload(
                    fileBytes,
                    ObjectUtils.asMap(
                            "folder", "avatars",
                            "public_id", "avatar_" + user.getId(),
                            "overwrite", "true"
                    )
            );
            String avatarUrl = upload.get("secure_url").toString();

            boolean updated = userService.updateAvatar(user.getId(), avatarUrl);

            if (updated) {
                User updatedUser = userService.getUserProfileById(user.getId()).orElse(user);
                session.setAttribute("user", updatedUser);

                out.write("{\"success\": true, \"url\": \"" + avatarUrl + "\"}");
            } else {
                out.write("{\"success\": false, \"message\": \"Lưu avatar thất bại\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"success\": false, \"message\": \"Upload thất bại, vui lòng thử lại\"}");
        }
    }
    private boolean isValidImageSignature(byte[] bytes) {
        if (bytes.length < 4) return false;
        if (bytes[0] == (byte)0x89 && bytes[1] == 0x50
                && bytes[2] == 0x4E && bytes[3] == 0x47) return true;
        if (bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8
                && bytes[2] == (byte)0xFF) return true;
        if (bytes[0] == 0x47 && bytes[1] == 0x49
                && bytes[2] == 0x46 && bytes[3] == 0x38) return true;
        return false;
    }
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
