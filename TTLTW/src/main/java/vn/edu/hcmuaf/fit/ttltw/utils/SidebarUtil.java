package vn.edu.hcmuaf.fit.ttltw.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.User;

public class SidebarUtil {
    public static User getUserFromRequestOrSession(HttpServletRequest request) {
        // Thử lấy từ request trước
        User user = (User) request.getAttribute("user");

        // Nếu không có trong request, lấy từ session
        if (user == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                user = (User) session.getAttribute("user");
            }
        }

        return user;
    }

    public static String getActiveMenu(HttpServletRequest request) {
        String activeMenu = (String) request.getAttribute("activeMenu");
        return activeMenu != null ? activeMenu : "profile";
    }

    public static String getAvatarPath(HttpServletRequest request, User user) {
        if (user == null || user.getAvatar() == null || user.getAvatar().isEmpty()) {
            return request.getContextPath() + "/assert/img/admin.jpg";
        }

        String avatar = user.getAvatar();
        if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
            return avatar;
        }
        if (avatar.startsWith("/")) {
            return request.getContextPath() + avatar;
        } else {
            return request.getContextPath() + "/" + avatar;
        }
    }

    public static void setSidebarData(HttpServletRequest request) {
        User user = getUserFromRequestOrSession(request);
        String activeMenu = getActiveMenu(request);
        String avatarPath = getAvatarPath(request, user);

        request.setAttribute("user", user);
        request.setAttribute("activeMenu", activeMenu);
        request.setAttribute("avatarPath", avatarPath);
    }
}
