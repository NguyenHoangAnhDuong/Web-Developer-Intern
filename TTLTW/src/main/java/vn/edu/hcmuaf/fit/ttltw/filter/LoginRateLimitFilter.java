package vn.edu.hcmuaf.fit.ttltw.filter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Rate-limit theo IP cho mọi endpoint liên quan đăng nhập. Bảo vệ chống brute-force diện rộng:
// account lockout chỉ chặn ở mức user; attacker vẫn có thể xoay vòng username để spam BCrypt
// hoặc enumerate. Bucket sliding-window đơn giản trong RAM, không cần external store.
@WebFilter(urlPatterns = {
        "/login",
        "/login-facebook",
        "/login-facebook-callback",
        "/login-google",
        "/login-google-callback"
})
public class LoginRateLimitFilter implements Filter {

    // Cho phép MAX_REQUESTS request mỗi WINDOW_MS cho một IP.
    // Giá trị mặc định 30/phút đủ rộng cho user thật (gõ sai vài lần + reload trang),
    // nhưng chặn được spam tự động.
    public static final int MAX_REQUESTS = 30;
    public static final long WINDOW_MS = 60_000L;

    // Một bucket cho mỗi IP. Reset count khi window cũ hết hạn (lazy, lúc tăng).
    private static final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        String ip = clientIp(http);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> new Bucket());

        long now = System.currentTimeMillis();
        int count;
        synchronized (bucket) {
            if (now - bucket.windowStart >= WINDOW_MS) {
                bucket.windowStart = now;
                bucket.count.set(0);
            }
            count = bucket.count.incrementAndGet();
        }

        if (count > MAX_REQUESTS) {
            long retryAfterSec = Math.max(1, (WINDOW_MS - (now - bucket.windowStart)) / 1000);
            httpResp.setStatus(429); // Too Many Requests
            httpResp.setHeader("Retry-After", String.valueOf(retryAfterSec));
            httpResp.setContentType("text/html; charset=UTF-8");
            httpResp.getWriter().write(
                    "<!doctype html><html><head><meta charset='UTF-8'><title>Quá nhiều yêu cầu</title></head>"
                            + "<body style='font-family:sans-serif;padding:40px;text-align:center'>"
                            + "<h2>Quá nhiều yêu cầu đăng nhập</h2>"
                            + "<p>IP của bạn đã vượt quá giới hạn " + MAX_REQUESTS + " yêu cầu/phút.</p>"
                            + "<p>Vui lòng thử lại sau " + retryAfterSec + " giây.</p>"
                            + "</body></html>");
            return;
        }

        chain.doFilter(req, resp);
    }

    // Lấy IP thật, tôn trọng X-Forwarded-For khi đứng sau reverse proxy.
    // Lưu ý: chỉ tin header này nếu app thực sự deploy sau proxy đáng tin cậy.
    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return req.getRemoteAddr();
    }

    // Reset toàn bộ buckets — chỉ dùng trong test.
    static void resetForTests() {
        buckets.clear();
    }

    private static class Bucket {
        long windowStart = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);
    }
}
