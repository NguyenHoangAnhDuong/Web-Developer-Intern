package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;

import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.LoginAuditService;
import vn.edu.hcmuaf.fit.ttltw.service.LoginResult;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;

@WebServlet("/login-google-callback")
public class LoginGoogleCallbackServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private final LoginAuditService auditService = new LoginAuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String clientId = (String) getServletContext().getAttribute("google.clientId");
        String clientSecret = (String) getServletContext().getAttribute("google.clientSecret");
        String redirectUri = (String) getServletContext().getAttribute("google.redirectUri");

        String code = request.getParameter("code");
        if (code == null || code.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login?error=Google login failed");
            return;
        }

        try {
            // 1. Đổi authorization code lấy access_token
            String tokenUrl = "https://oauth2.googleapis.com/token";
            String tokenParams = "code=" + code
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&redirect_uri=" + redirectUri
                    + "&grant_type=authorization_code";

            String tokenResponse = sendPost(tokenUrl, tokenParams);
            JsonObject tokenJson = JsonParser.parseString(tokenResponse).getAsJsonObject();
            String accessToken = tokenJson.get("access_token").getAsString();

            // 2. Lấy thông tin user từ Google
            String infoUrl = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
            String infoResponse = sendGet(infoUrl);
            JsonObject info = JsonParser.parseString(infoResponse).getAsJsonObject();

            String googleId = info.get("id").getAsString();
            String email = info.has("email") ? info.get("email").getAsString() : null;
            String name = info.has("name") ? info.get("name").getAsString() : "Google User";
            String avatar = info.has("picture") ? info.get("picture").getAsString() : null;

            if (email == null || email.isEmpty()) {
                response.sendRedirect(request.getContextPath()
                        + "/login?error=Không lấy được email từ Google");
                return;
            }

            // 3. Tạo username từ tên (bỏ dấu + thêm 4 số cuối googleId)
            String baseUsername = removeDiacritics(name)
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
            if (baseUsername.isEmpty()) {
                baseUsername = "gguser";
            }
            String username = baseUsername + googleId.substring(googleId.length() - Math.min(4, googleId.length()));

            // 4. Tạo User object cho Google. provider/providerId được set vào User
            // để UserService.loginOrRegisterSocial truyền sang DAO; record thật được lưu
            // ở bảng user_social_accounts (1 user có thể link nhiều provider).
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setFirstName(name);
            u.setAvatar(avatar);
            u.setRolesId(2);        // Mặc định là khách hàng
            u.setStatus(1);         // Hoạt động
            u.setProvider("google");
            u.setProviderId(googleId);

            // 5. Kiểm tra email tồn tại → đăng nhập; chưa có → tạo mới
            LoginResult result = userService.loginOrRegisterSocial(u);
            auditService.log(request, email, LoginAuditService.CHANNEL_GOOGLE, result);

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
                                    "Đăng nhập Google thất bại, vui lòng thử lại",
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

            // 6. Xử lý redirect URL
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
                redirectUrl = "/home";
            }

            if (redirectUrl.startsWith(request.getContextPath())) {
                redirectUrl = redirectUrl.substring(request.getContextPath().length());
            }

            response.sendRedirect(contextPath + redirectUrl);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/login?error=Google login error");
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

    // Google token endpoint yêu cầu POST
    private String sendPost(String urlStr, String params) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }

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
