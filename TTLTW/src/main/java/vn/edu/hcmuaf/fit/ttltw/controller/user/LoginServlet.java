
package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;

import java.io.IOException;


@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    private UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lưu URL hiện tại vào session để quay về sau khi đăng nhập
        String referer = request.getHeader("Referer");
        String redirectParam = request.getParameter("redirect");
        String message = request.getParameter("message");
        HttpSession session = request.getSession();

        // Nếu có message=register_success, không lưu redirect URL (sẽ về home)
        if (!"register_success".equals(message)) {
            // Ưu tiên parameter, nếu không có thì dùng referer
            String currentUrl = (redirectParam != null && !redirectParam.isEmpty()) ? redirectParam : (referer != null ? referer : null);

            // Chỉ lưu nếu URL hợp lệ (không phải trang login, logout, register)
            if (currentUrl != null && !currentUrl.contains("/login")
                    && !currentUrl.contains("/logout") && !currentUrl.contains("/register")) {
                String contextPath = request.getContextPath();
                String relativeUrl = extractRelativePath(currentUrl, contextPath);
                if (relativeUrl != null && !relativeUrl.isEmpty() && !relativeUrl.equals("/login")) {
                    session.setAttribute("loginRedirectUrl", relativeUrl);
                }
            }
        }

        request.getRequestDispatcher("/views/user/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String input = request.getParameter("input");
        String password = request.getParameter("password");

        User user = userService.login(input, password);

        if (user == null) {
            request.setAttribute("error", "Sai tài khoản hoặc mật khẩu!");
            request.setAttribute("inputValue", input);
            request.getRequestDispatcher("/views/user/login.jsp").forward(request, response);
            return;
        }
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());

        int role = user.getRole();
        String contextPath = request.getContextPath();

        // Nếu vừa đăng xuất trước đó, buộc quay về trang home (chỉ áp dụng user)
        boolean justLoggedOut = consumeJustLoggedOutCookie(request, response);

        if (role == 0) {
            response.sendRedirect(contextPath + "/admin/dashboard");
            return;
        }
        String redirectUrl = null;
        // Nếu vừa logout → login lại thì đưa thẳng về home
        if (justLoggedOut) {
            redirectUrl = "/home";
        }
        if (redirectUrl == null) {
            String savedRedirectUrl = (String) session.getAttribute("redirectUrl");
            if (savedRedirectUrl != null) {
                session.removeAttribute("redirectUrl");
                redirectUrl = savedRedirectUrl;
            }
        }
        if (redirectUrl == null) {
            String loginRedirectUrl = (String) session.getAttribute("loginRedirectUrl");
            if (loginRedirectUrl != null) {
                session.removeAttribute("loginRedirectUrl");
                redirectUrl = loginRedirectUrl;
            }
        }
        // Mặc định cho user
        if (redirectUrl == null) {
            redirectUrl = "/home";
        }
        // Đảm bảo redirectUrl không chứa contextPath
        if (redirectUrl.startsWith(request.getContextPath())) {
            redirectUrl = redirectUrl.substring(request.getContextPath().length());
        }

        response.sendRedirect(contextPath + redirectUrl);
    }
    private boolean consumeJustLoggedOutCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isEmpty()) {
            contextPath = "/";
        }
        boolean found = false;
        for (Cookie cookie : cookies) {
            if ("justLoggedOut".equals(cookie.getName())) {
                found = true;
                // Xóa cookie sau khi sử dụng
                cookie.setValue("");
                cookie.setMaxAge(0);
                cookie.setPath(contextPath);
                response.addCookie(cookie);
                break;
            }
        }
        return found;
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
