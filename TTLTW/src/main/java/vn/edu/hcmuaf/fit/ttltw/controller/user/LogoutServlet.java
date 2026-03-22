package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.hcmuaf.fit.ttltw.model.User;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", value = "/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        int role = -1;
        if (session != null) {
            Object userObj = session.getAttribute("user");
            if (userObj instanceof User) {
                role = ((User) userObj).getRolesId();
            }
        }
        // Lấy URL hiện tại từ referer hoặc parameter
        String referer = request.getHeader("Referer");
        String currentUrl = request.getParameter("redirect");
        String previousUrl = (currentUrl != null && !currentUrl.isEmpty())
                ? currentUrl
                : (referer != null ? referer : null);

        if (session != null) {
            session.invalidate();
        }
        // Đánh dấu trạng thái vừa đăng xuất để buộc lần đăng nhập tiếp theo về trang home (chỉ áp dụng user)
        if (role != 0 && role != -1) {
            setJustLoggedOutCookie(request, response);
        }
        String contextPath = request.getContextPath();
        String redirectUrl;

        // Nếu không có URL trước đó, mặc định về home
        if (previousUrl == null || previousUrl.isEmpty()) {
            redirectUrl = contextPath + "/home";
        } else {
            // Trích xuất path từ URL (có thể là URL đầy đủ hoặc relative path)
            String relativeUrl = extractRelativePath(previousUrl, contextPath);

            // Nếu không thể trích xuất path hợp lệ, về home
            if (relativeUrl == null || relativeUrl.isEmpty() || !relativeUrl.startsWith("/")) {
                redirectUrl = contextPath + "/home";
            } else {
                // Loại bỏ query string để kiểm tra path
                String pathWithoutQuery = relativeUrl.split("\\?")[0];

                // Kiểm tra xem trang có bắt buộc đăng nhập không
                // Các trang bắt buộc đăng nhập: /cart, /checkout, /profile, /user/*, /admin/*
                boolean requiresLogin = pathWithoutQuery.startsWith("/cart")
                        || pathWithoutQuery.startsWith("/checkout")
                        || pathWithoutQuery.startsWith("/profile")
                        || pathWithoutQuery.startsWith("/user/")
                        || pathWithoutQuery.startsWith("/admin/");

                if (requiresLogin) {
                    // Trang bắt buộc đăng nhập
                    if (pathWithoutQuery.startsWith("/admin/")) {
                        // Trang admin -> về login
                        redirectUrl = contextPath + "/login";
                    } else {
                        // Trang user bắt buộc đăng nhập -> về home
                        redirectUrl = contextPath + "/home";
                    }
                } else {
                    // Trang không bắt buộc đăng nhập -> giữ nguyên trang (bao gồm query string nếu có)
                    redirectUrl = contextPath + relativeUrl;
                }
            }
        }
        response.sendRedirect(redirectUrl);
    }
    private void setJustLoggedOutCookie(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getContextPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        Cookie logoutFlag = new Cookie("justLoggedOut", "true");
        logoutFlag.setPath(path);
        logoutFlag.setHttpOnly(true);
        logoutFlag.setMaxAge(300); // 5 phút đủ để thao tác đăng nhập lại
        response.addCookie(logoutFlag);
    }
    private String extractRelativePath(String url, String contextPath) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // Nếu là URL đầy đủ (http:// hoặc https://)
            if (url.startsWith("http://") || url.startsWith("https://")) {
                java.net.URL urlObj = new java.net.URL(url);
                String path = urlObj.getPath();

                // Loại bỏ context path nếu có
                if (path.startsWith(contextPath)) {
                    return path.substring(contextPath.length());
                }
                return path;
            }

            // Nếu đã chứa context path, loại bỏ nó
            if (url.startsWith(contextPath)) {
                return url.substring(contextPath.length());
            }

            // Nếu đã là relative path, trả về nguyên
            if (url.startsWith("/")) {
                return url;
            }

            // Trường hợp khác, thêm / ở đầu
            return "/" + url;
        } catch (Exception e) {
            // Nếu có lỗi parse URL, thử cách đơn giản hơn
            if (url.contains(contextPath)) {
                int index = url.indexOf(contextPath);
                return url.substring(index + contextPath.length());
            }
            return url.startsWith("/") ? url : "/" + url;
        }
    }
}
