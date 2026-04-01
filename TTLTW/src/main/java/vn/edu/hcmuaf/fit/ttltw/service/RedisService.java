package vn.edu.hcmuaf.fit.ttltw.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;
import vn.edu.hcmuaf.fit.ttltw.config.RedisConnect;
import vn.edu.hcmuaf.fit.ttltw.model.Product;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RedisService {

    // Gson với adapter cho LocalDateTime (Gson không tự hỗ trợ)
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                            LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    // ── Key builders ────────────────────────────────────────────────────────────
    private static String otpKey(String email)            { return "otp:" + email; }
    private static String productKey(int id)              { return "product:" + id; }
    private static String productListKey(String suffix)   { return "products:" + suffix; }

    // ── OTP ─────────────────────────────────────────────────────────────────────

    /**
     * Lưu OTP cho email, tự hết hạn sau redis.ttl.otp giây (mặc định 5 phút).
     */
    public static void saveOtp(String email, String otp) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.setex(otpKey(email), RedisConnect.getTtlOtp(), otp);
        }
    }

    /**
     * Lấy OTP đang còn hiệu lực. Trả về null nếu đã hết hạn hoặc không tồn tại.
     */
    public static String getOtp(String email) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            return jedis.get(otpKey(email));
        }
    }

    /**
     * Xoá OTP ngay sau khi xác thực thành công.
     */
    public static void deleteOtp(String email) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.del(otpKey(email));
        }
    }

    // ── Product cache ────────────────────────────────────────────────────────────

    /**
     * Cache một sản phẩm theo id. TTL = redis.ttl.product giây (mặc định 30 phút).
     */
    public static void cacheProduct(Product product) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.setex(productKey(product.getId()),
                    RedisConnect.getTtlProduct(),
                    GSON.toJson(product));
        }
    }

    /**
     * Lấy sản phẩm từ cache. Trả về null nếu không có hoặc đã hết hạn (cache miss).
     */
    public static Product getCachedProduct(int id) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            String json = jedis.get(productKey(id));
            return json != null ? GSON.fromJson(json, Product.class) : null;
        }
    }

    /**
     * Cache danh sách sản phẩm với key tùy ý.
     * Ví dụ: cacheProductList("all", list) → key = "products:all"
     *         cacheProductList("category:1", list) → key = "products:category:1"
     */
    public static void cacheProductList(String suffix, List<Product> products) {
        Type listType = new TypeToken<List<Product>>() {}.getType();
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.setex(productListKey(suffix),
                    RedisConnect.getTtlProduct(),
                    GSON.toJson(products, listType));
        }
    }

    /**
     * Lấy danh sách sản phẩm từ cache. Trả về null nếu cache miss.
     */
    public static List<Product> getCachedProductList(String suffix) {
        Type listType = new TypeToken<List<Product>>() {}.getType();
        try (Jedis jedis = RedisConnect.getConnection()) {
            String json = jedis.get(productListKey(suffix));
            return json != null ? GSON.fromJson(json, listType) : null;
        }
    }

    /**
     * Xoá cache một sản phẩm (gọi khi admin cập nhật / xoá sản phẩm đó).
     */
    public static void evictProduct(int id) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.del(productKey(id));
        }
    }

    /**
     * Xoá cache danh sách sản phẩm (gọi khi admin thêm / sửa / xoá bất kỳ sản phẩm nào).
     */
    public static void evictProductList(String suffix) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.del(productListKey(suffix));
        }
    }
}
