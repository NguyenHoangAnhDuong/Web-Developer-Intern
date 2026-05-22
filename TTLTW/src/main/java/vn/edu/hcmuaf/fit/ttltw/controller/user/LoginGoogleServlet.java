package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/login-google")
public class LoginGoogleServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String clientId = (String) getServletContext().getAttribute("google.clientId");
        String redirectUri = (String) getServletContext().getAttribute("google.redirectUri");

        if (clientId == null || clientId.isBlank()
                || redirectUri == null || redirectUri.isBlank()) {
            getServletContext().log(
                    "[LoginGoogleServlet] Thiếu cấu hình Google OAuth. "
                            + "Kiểm tra file .env tại project root có GOOGLE_CLIENT_ID và GOOGLE_REDIRECT_URI. "
                            + "clientIdPresent=" + (clientId != null && !clientId.isBlank())
                            + ", redirectUriPresent=" + (redirectUri != null && !redirectUri.isBlank()));
            resp.sendRedirect(req.getContextPath()
                    + "/login?error=" + URLEncoder.encode(
                            "Đăng nhập Google chưa được cấu hình. Vui lòng liên hệ quản trị viên.",
                            StandardCharsets.UTF_8));
            return;
        }

        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8);

        resp.sendRedirect(url);
    }
}
