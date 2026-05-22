package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;

@WebServlet("/login-facebook")
public class LoginFacebookServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String CLIENT_ID = (String) getServletContext().getAttribute("facebook.clientId");
        String CLIENT_SECRET = (String) getServletContext().getAttribute("facebook.clientSecret");
        String REDIRECT_URI = (String) getServletContext().getAttribute("facebook.redirectUri");

        String url = "https://www.facebook.com/v18.0/dialog/oauth"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                + "&scope=email,public_profile";

        resp.sendRedirect(url);
    }
}
