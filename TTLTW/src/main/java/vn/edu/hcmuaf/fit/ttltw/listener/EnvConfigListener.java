package vn.edu.hcmuaf.fit.ttltw.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import vn.edu.hcmuaf.fit.ttltw.config.EnvLoader;

/**
 * Lúc khởi động web app, đọc các secret từ .env và bind vào ServletContext attributes.
 * Servlet đọc qua getServletContext().getAttribute(...) — giữ cùng tên key như web.xml cũ
 * để hạn chế sửa nơi gọi.
 */
@WebListener
public class EnvConfigListener implements ServletContextListener {

    private static final String[][] MAPPINGS = {
            { "facebook.clientId",     "FACEBOOK_CLIENT_ID" },
            { "facebook.clientSecret", "FACEBOOK_CLIENT_SECRET" },
            { "facebook.redirectUri",  "FACEBOOK_REDIRECT_URI" },
            { "google.clientId",       "GOOGLE_CLIENT_ID" },
            { "google.clientSecret",   "GOOGLE_CLIENT_SECRET" },
            { "google.redirectUri",    "GOOGLE_REDIRECT_URI" },
    };

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        for (String[] m : MAPPINGS) {
            String value = EnvLoader.get(m[1]);
            if (value == null || value.isBlank()) {
                value = ctx.getInitParameter(m[0]);
            }
            if (value != null && !value.isBlank()) {
                ctx.setAttribute(m[0], value);
            }
        }
    }
}
