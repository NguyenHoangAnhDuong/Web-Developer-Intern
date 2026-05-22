
package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.LoginAuditService;
import vn.edu.hcmuaf.fit.ttltw.service.LoginResult;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;
import vn.edu.hcmuaf.fit.ttltw.utils.PermissionUtil;


@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    private UserService userService = new UserService();
    private final LoginAuditService auditService = new LoginAuditService();

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
                    && !currentUrl.contains("/logout") && !currentUrl.contains("/register") && !currentUrl.contains("/change-password")) {
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

        LoginResult result = userService.login(input, password);
        auditService.log(request, input, LoginAuditService.CHANNEL_PASSWORD, result);

        switch (result.getStatus()) {
            case INVALID_CREDENTIALS -> {
                String msg = "Sai tài khoản hoặc mật khẩu!";
                if (result.getRemainingAttempts() > 0 && result.getRemainingAttempts() < UserService.MAX_FAILED_ATTEMPTS) {
                    msg += " Bạn còn " + result.getRemainingAttempts() + " lần thử trước khi tài khoản bị khóa tạm thời.";
                }
                request.setAttribute("error", msg);
                request.setAttribute("inputValue", input);
                request.getRequestDispatcher("/views/user/login.jsp").forward(request, response);
                return;
            }
            case ACCOUNT_LOCKED -> {
                request.setAttribute("error",
                        "Tài khoản đã bị khóa tạm thời do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau "
                                + formatDuration(result.getLockSecondsRemaining()) + ".");
                request.setAttribute("inputValue", input);
                request.getRequestDispatcher("/views/user/login.jsp").forward(request, response);
                return;
            }
            case ACCOUNT_INACTIVE -> {
                request.setAttribute("error", "Tài khoản đã bị khóa, vui lòng liên hệ quản trị viên.");
                request.setAttribute("inputValue", input);
                request.getRequestDispatcher("/views/user/login.jsp").forward(request, response);
                return;
            }
            default -> { /* SUCCESS — đi tiếp */ }
        }
        User user = result.getUser();
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRolesId());
        // Nạp quyền vào session để dùng cho toàn bộ phiên làm việc
        PermissionUtil.loadAndCache(session, user);

        String contextPath = request.getContextPath();

        // Nếu vừa đăng xuất trước đó, buộc quay về trang home (chỉ áp dụng user)
        boolean justLoggedOut = consumeJustLoggedOutCookie(request, response);

        // Tất cả user không phải customer đều vào trang admin
        if (!PermissionUtil.isCustomer(session)) {
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

    // Hiển thị thời lượng khóa thân thiện: "5 phút 12 giây" / "47 giây".
    static String formatDuration(long seconds) {
        if (seconds <= 0) return "vài giây";
        long minutes = seconds / 60;
        long remainder = seconds % 60;
        if (minutes <= 0) return remainder + " giây";
        if (remainder == 0) return minutes + " phút";
        return minutes + " phút " + remainder + " giây";
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
