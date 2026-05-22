package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.LoginAuditService;
import vn.edu.hcmuaf.fit.ttltw.service.LoginResult;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;

@WebServlet("/login-facebook-callback")
public class LoginFacebookCallbackServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private final LoginAuditService auditService = new LoginAuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String CLIENT_ID = (String) getServletContext().getAttribute("facebook.clientId");
        String CLIENT_SECRET = (String) getServletContext().getAttribute("facebook.clientSecret");
        String REDIRECT_URI = (String) getServletContext().getAttribute("facebook.redirectUri");

        String code = request.getParameter("code");
        if (code == null || code.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login?error=Facebook login failed");
            return;
        }
        try {
            // Lấy access_token
            String tokenUrl = "https://graph.facebook.com/v18.0/oauth/access_token"
                    + "?client_id=" + CLIENT_ID
                    + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                    + "&client_secret=" + CLIENT_SECRET
                    + "&code=" + code;

            String tokenResponse = sendGet(tokenUrl);
            JsonObject tokenJson = JsonParser.parseString(tokenResponse).getAsJsonObject();
            String accessToken = tokenJson.get("access_token").getAsString();

            // Lấy thông tin user (bao gồm email — đã xin quyền 'email' ở bước authorize)
            String infoUrl = "https://graph.facebook.com/me"
                    + "?fields=id,name,email,picture.type(large)"
                    + "&access_token=" + accessToken;

            String infoResponse = sendGet(infoUrl);
            JsonObject info = JsonParser.parseString(infoResponse).getAsJsonObject();

            String fbId = info.get("id").getAsString();
            String name = info.has("name") ? info.get("name").getAsString() : "Facebook User";

            // Email là bắt buộc cho flow đăng nhập/đăng ký theo email.
            // Người dùng có thể từ chối cấp quyền email khi authorize — khi đó Graph API không trả về field 'email'.
            String email = (info.has("email") && !info.get("email").isJsonNull())
                    ? info.get("email").getAsString()
                    : null;
            if (email == null || email.isBlank()) {
                response.sendRedirect(request.getContextPath()
                        + "/login?error=" + URLEncoder.encode(
                                "Bạn cần cấp quyền truy cập email để đăng nhập bằng Facebook",
                                StandardCharsets.UTF_8));
                return;
            }

            String avatar = null;
            if (info.has("picture")) {
                avatar = info.getAsJsonObject("picture")
                        .getAsJsonObject("data")
                        .get("url").getAsString();
            }

            // Sinh username gợi ý cho trường hợp tạo account mới (UserService sẽ tự dedupe nếu trùng)
            String baseUsername = removeDiacritics(name)
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
            if (baseUsername.isEmpty()) {
                baseUsername = "fbuser";
            }
            String username = baseUsername + fbId.substring(Math.max(0, fbId.length() - 4));

            // Đẩy thông tin từ Facebook sang service để quyết định login hay register
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setFirstName(name);
            u.setAvatar(avatar);
            u.setRolesId(2);     // Mặc định là khách hàng
            u.setStatus(1);      // Hoạt động
            u.setProvider("facebook");
            u.setProviderId(fbId);

            LoginResult result = userService.loginOrRegisterSocial(u);
            auditService.log(request, email, LoginAuditService.CHANNEL_FACEBOOK, result);

            switch (result.getStatus()) {
                case ACCOUNT_LOCKED -> {
                    redirectWithToast(request, response,
                            "Tài khoản đã bị khóa tạm thời do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau "
                                    + LoginServlet.formatDuration(result.getLockSecondsRemaining()) + ".");
                    return;
                }
                case ACCOUNT_INACTIVE -> {
                    redirectWithToast(request, response,
                            "Tài khoản đã bị khóa, vui lòng liên hệ quản trị viên.");
                    return;
                }
                case SOCIAL_LOGIN_FAILED, INVALID_CREDENTIALS -> {
                    response.sendRedirect(request.getContextPath()
                            + "/login?error=" + URLEncoder.encode(
                                    "Đăng nhập Facebook thất bại, vui lòng thử lại",
                                    StandardCharsets.UTF_8));
                    return;
                }
                default -> { /* SUCCESS — đi tiếp */ }
            }
            User user = result.getUser();

            HttpSession session = request.getSession();

            // Chỉ cho phép khách hàng (role == 2)
            if (user.getRolesId() != 2) {
                session.invalidate();
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("toastMessage", "Nhân viên vui lòng sử dụng đăng nhập thường");
                newSession.setAttribute("toastType", "error");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            session.setAttribute("user", user);
            session.setAttribute("role", user.getRolesId());

            int role = user.getRolesId();
            String contextPath = request.getContextPath();
            String redirectUrl = null;

            // Ưu tiên 1: redirect từ LoginFilter
            String savedRedirectUrl = (String) session.getAttribute("redirectUrl");
            if (savedRedirectUrl != null) {
                session.removeAttribute("redirectUrl");

                String pathWithoutQuery = savedRedirectUrl.split("\\?")[0];
                boolean isValid = true;

                if (role == 0 && (pathWithoutQuery.startsWith("/cart")
                        || pathWithoutQuery.startsWith("/checkout")
                        || pathWithoutQuery.startsWith("/profile")
                        || pathWithoutQuery.startsWith("/user/"))) {
                    isValid = false;
                }

                if (role == 1 && pathWithoutQuery.startsWith("/admin/")) {
                    isValid = false;
                }

                if (isValid) {
                    redirectUrl = savedRedirectUrl;
                }
            }
                // Ưu tiên 2: trang trước khi vào login
            if (redirectUrl == null) {
                String loginRedirectUrl = (String) session.getAttribute("loginRedirectUrl");
                if (loginRedirectUrl != null) {
                    session.removeAttribute("loginRedirectUrl");

                    String pathWithoutQuery = loginRedirectUrl.split("\\?")[0];
                    boolean isValid = true;

                    if (role == 0 && (pathWithoutQuery.startsWith("/cart")
                            || pathWithoutQuery.startsWith("/checkout")
                            || pathWithoutQuery.startsWith("/profile")
                            || pathWithoutQuery.startsWith("/user/"))) {
                        isValid = false;
                    }

                    if (role == 1 && pathWithoutQuery.startsWith("/admin/")) {
                        isValid = false;
                    }

                    if (pathWithoutQuery.equals("/login") || pathWithoutQuery.equals("/logout")) {
                        isValid = false;
                    }

                    if (isValid) {
                        redirectUrl = loginRedirectUrl;
                    }
                }
            }
            // Mặc định
            if (redirectUrl == null) {
                redirectUrl = "/home"; // vì role luôn là 2 rồi
            }
            // Đảm bảo redirectUrl không chứa contextPath
            if (redirectUrl.startsWith(request.getContextPath())) {
                redirectUrl = redirectUrl.substring(request.getContextPath().length());
            }
            response.sendRedirect(contextPath + redirectUrl);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/login?error=Facebook login error");
        }
    }
    // Invalidate session hiện tại rồi gắn toast vào session mới để hiển thị tại /login.
    private void redirectWithToast(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        HttpSession current = request.getSession(false);
        if (current != null) current.invalidate();
        HttpSession fresh = request.getSession(true);
        fresh.setAttribute("toastMessage", message);
        fresh.setAttribute("toastType", "error");
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private String sendGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
    private String removeDiacritics(String input) {
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
