package vn.edu.hcmuaf.fit.ttltw.dao;

import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;

// DAO ghi audit log mọi lần đăng nhập. Insert không bao giờ throw ra ngoài —
// audit fail không được làm vỡ login flow.
public class LoginAttemptDao {

    private static final int MAX_INPUT_LEN = 255;
    private static final int MAX_UA_LEN = 500;

    public void insert(Integer userId, String input, String ip, String userAgent,
                       String channel, String outcome) {
        try {
            DBConnect.getJdbi().useHandle(h -> h.createUpdate("""
                            INSERT INTO login_attempts (user_id, input, ip, user_agent, channel, outcome)
                            VALUES (:uid, :input, :ip, :ua, :ch, :out)
                            """)
                    .bind("uid", userId)
                    .bind("input", truncate(input, MAX_INPUT_LEN))
                    .bind("ip", truncate(ip, 45))
                    .bind("ua", truncate(userAgent, MAX_UA_LEN))
                    .bind("ch", channel)
                    .bind("out", outcome)
                    .execute());
        } catch (Exception e) {
            // Audit phải im lặng khi DB hỏng — không cản trở user đăng nhập.
            System.err.println("[LoginAttemptDao] insert failed: " + e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
