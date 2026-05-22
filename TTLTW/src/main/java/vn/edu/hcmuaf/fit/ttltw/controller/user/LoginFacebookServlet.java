package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/login-facebook")
public class LoginFacebookServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String clientId = (String) getServletContext().getAttribute("facebook.clientId");
        String redirectUri = (String) getServletContext().getAttribute("facebook.redirectUri");

        if (clientId == null || clientId.isBlank()
                || redirectUri == null || redirectUri.isBlank()) {
            getServletContext().log(
                    "[LoginFacebookServlet] Thiếu cấu hình Facebook OAuth. "
                            + "Kiểm tra .env có FACEBOOK_CLIENT_ID và FACEBOOK_REDIRECT_URI. "
                            + "clientIdPresent=" + (clientId != null && !clientId.isBlank())
                            + ", redirectUriPresent=" + (redirectUri != null && !redirectUri.isBlank()));
            resp.sendRedirect(req.getContextPath()
                    + "/login?error=" + URLEncoder.encode(
                            "Đăng nhập Facebook chưa được cấu hình. Vui lòng liên hệ quản trị viên.",
                            StandardCharsets.UTF_8));
            return;
        }

        String url = "https://www.facebook.com/v18.0/dialog/oauth"
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("email,public_profile", StandardCharsets.UTF_8);

        resp.sendRedirect(url);
    }
}
