package vn.edu.hcmuaf.fit.ttltw.service;

import jakarta.servlet.http.HttpServletRequest;
import vn.edu.hcmuaf.fit.ttltw.dao.LoginAttemptDao;

// Helper xử lý audit từ controller. Tách khỏi UserService để UserService không phụ thuộc
// vào HttpServletRequest. Controller gọi sau khi service trả LoginResult.
public class LoginAuditService {

    public static final String CHANNEL_PASSWORD = "PASSWORD";
    public static final String CHANNEL_FACEBOOK = "FACEBOOK";
    public static final String CHANNEL_GOOGLE = "GOOGLE";

    private final LoginAttemptDao dao = new LoginAttemptDao();

    public void log(HttpServletRequest req, String input, String channel, LoginResult result) {
        Integer userId = (result.getUser() != null) ? result.getUser().getId() : null;
        dao.insert(userId, input, clientIp(req), req.getHeader("User-Agent"),
                channel, result.getStatus().name());
    }

    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return req.getRemoteAddr();
    }
}
