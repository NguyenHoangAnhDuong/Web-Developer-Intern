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
import vn.edu.hcmuaf.fit.ttltw.service.UserService;

@WebServlet("/login-facebook-callback")
public class LoginFacebookCallbackServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String CLIENT_ID = getServletContext().getInitParameter("facebook.clientId");
        String CLIENT_SECRET = getServletContext().getInitParameter("facebook.clientSecret");
        String REDIRECT_URI = getServletContext().getInitParameter("facebook.redirectUri");

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

            // Lấy thông tin user
            String infoUrl = "https://graph.facebook.com/me"
                    + "?fields=id,name,email,picture.type(large)"
                    + "&access_token=" + accessToken;

            String infoResponse = sendGet(infoUrl);
            JsonObject info = JsonParser.parseString(infoResponse).getAsJsonObject();

            String fbId = info.get("id").getAsString();
            String name = info.has("name") ? info.get("name").getAsString() : "Facebook User";
            String fakeEmail = "fb_" + fbId + "@facebook.local";

            String avatar = null;
            if (info.has("picture")) {
                avatar = info.getAsJsonObject("picture")
                        .getAsJsonObject("data")
                        .get("url").getAsString();
            }

            // Hàm bỏ dấu + chuẩn hóa
            String baseUsername = removeDiacritics(name)
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", ""); // bỏ ký tự lạ & khoảng trắng

            // tránh rỗng
            if (baseUsername.isEmpty()) {
                baseUsername = "fbuser";
            }

            // gắn thêm 4 số cuối của fbId để tránh trùng
            String username = baseUsername + fbId.substring(fbId.length() - 4);

            // 3. Tạo User từ Facebook
            User u = new User();
            u.setUsername(username);
            u.setEmail(fakeEmail);
            u.setFirstName(name);
            u.setAvatar(avatar);
            u.setRolesId(2);     // Mặc định là khách hàng
            u.setStatus(1);      // Hoạt động
            u.setProvider("facebook");
            u.setProviderId(fbId);

            User user = userService.loginOrRegisterSocial(u);

            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login?error=Facebook login failed");
                return;
            }

            HttpSession session = request.getSession();

            // Chỉ cho phép khách hàng (role == 2)
            if (user.getRolesId() != 2) {
                session.invalidate();
                response.sendRedirect(request.getContextPath()
                        + "/login?error=Only user accounts can login with Facebook");
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
