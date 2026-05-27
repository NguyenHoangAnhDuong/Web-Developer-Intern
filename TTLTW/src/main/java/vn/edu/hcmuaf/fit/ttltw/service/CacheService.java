package vn.edu.hcmuaf.fit.ttltw.service;

import java.time.Duration;
import java.util.List;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import vn.edu.hcmuaf.fit.ttltw.model.Product;
import vn.edu.hcmuaf.fit.ttltw.model.User;

/**
 * Cache in-memory thay thế Redis cho quy mô đồ án — dữ liệu mất khi restart Tomcat
 * (chấp nhận được với OTP/pending user/cache sản phẩm).
 *
 * Mỗi loại dữ liệu dùng một Cache riêng vì Caffeine cố định một mức
 * expireAfterWrite cho cả cache (không set TTL theo từng key như Redis SETEX).
 */
public class CacheService {

    private static final Duration TTL_OTP          = Duration.ofMinutes(5);
    private static final Duration TTL_PENDING_USER = Duration.ofMinutes(10);
    private static final Duration TTL_RESET_TOKEN  = Duration.ofMinutes(10);
    private static final Duration TTL_PRODUCT      = Duration.ofMinutes(30);

    // OTP đăng ký và OTP reset dùng chung cache (cùng TTL 5 phút) —
    // tách namespace bằng prefix key để cùng một email không bị xung đột.
    private static final Cache<String, String> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(TTL_OTP)
            .maximumSize(10_000)
            .build();

    private static final Cache<String, User> pendingUserCache = Caffeine.newBuilder()
            .expireAfterWrite(TTL_PENDING_USER)
            .maximumSize(10_000)
            .build();

    private static final Cache<String, String> resetTokenCache = Caffeine.newBuilder()
            .expireAfterWrite(TTL_RESET_TOKEN)
            .maximumSize(10_000)
            .build();

    private static final Cache<Integer, Product> productCache = Caffeine.newBuilder()
            .expireAfterWrite(TTL_PRODUCT)
            .maximumSize(1_000)
            .build();

    private static final Cache<String, List<Product>> productListCache = Caffeine.newBuilder()
            .expireAfterWrite(TTL_PRODUCT)
            .maximumSize(100)
            .build();

    // ============== OTP đăng ký ==============

    public static void saveOtp(String email, String otp) {
        otpCache.put("otp:" + email, otp);
    }

    public static String getOtp(String email) {
        return otpCache.getIfPresent("otp:" + email);
    }

    public static void deleteOtp(String email) {
        otpCache.invalidate("otp:" + email);
    }

    // ============== Pending user (chờ verify OTP đăng ký) ==============

    public static void savePendingUser(String email, User user) {
        pendingUserCache.put(email, user);
    }

    public static User getPendingUser(String email) {
        return pendingUserCache.getIfPresent(email);
    }

    public static void deletePendingUser(String email) {
        pendingUserCache.invalidate(email);
    }

    // ============== OTP quên mật khẩu ==============

    public static void saveResetOtp(String email, String otp) {
        otpCache.put("reset_otp:" + email, otp);
    }

    public static String getResetOtp(String email) {
        return otpCache.getIfPresent("reset_otp:" + email);
    }

    public static void deleteResetOtp(String email) {
        otpCache.invalidate("reset_otp:" + email);
    }

    // ============== Reset token (vé một lần sau khi OTP đã verify) ==============

    public static void saveResetToken(String token, String email) {
        resetTokenCache.put(token, email);
    }

    public static String getResetTokenEmail(String token) {
        return resetTokenCache.getIfPresent(token);
    }

    public static void deleteResetToken(String token) {
        resetTokenCache.invalidate(token);
    }

    // ============== Cache sản phẩm ==============

    public static void cacheProduct(Product product) {
        productCache.put(product.getId(), product);
    }

    public static Product getCachedProduct(int id) {
        return productCache.getIfPresent(id);
    }

    public static void cacheProductList(String suffix, List<Product> products) {
        productListCache.put(suffix, products);
    }

    public static List<Product> getCachedProductList(String suffix) {
        return productListCache.getIfPresent(suffix);
    }

    public static void evictProduct(int id) {
        productCache.invalidate(id);
    }

    public static void evictProductList(String suffix) {
        productListCache.invalidate(suffix);
    }
}
