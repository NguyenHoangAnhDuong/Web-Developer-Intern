package vn.edu.hcmuaf.fit.ttltw.service;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import redis.clients.jedis.Jedis;
import vn.edu.hcmuaf.fit.ttltw.config.RedisConnect;
import vn.edu.hcmuaf.fit.ttltw.model.Product;
import vn.edu.hcmuaf.fit.ttltw.model.User;

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

    //  Key lưu trữ trong Redis
    private static String otpKey(String email)            { return "otp:" + email; }
    private static String pendingUserKey(String email)    { return "pending_user:" + email; }
    private static String productKey(int id)              { return "product:" + id; }
    private static String productListKey(String suffix)   { return "products:" + suffix; }


    //Lưu OTP cho email, tự hết hạn sau 5 phút.

    public static void saveOtp(String email, String otp) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.setex(otpKey(email), RedisConnect.getTtlOtp(), otp);
        }
    }

    
    public static String getOtp(String email) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            return jedis.get(otpKey(email));
        }
    }

    
    public static void deleteOtp(String email) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.del(otpKey(email));
        }
    }

    //Lưu thông tin user chờ xác thực vào Redis, tự hết hạn sau 10 phút.
    public static void savePendingUser(String email, User user) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.setex(pendingUserKey(email), RedisConnect.getTtlPendingUser(), GSON.toJson(user));
        }
    }

    
    public static User getPendingUser(String email) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            String json = jedis.get(pendingUserKey(email));
            return json != null ? GSON.fromJson(json, User.class) : null;
        }
    }

    
    public static void deletePendingUser(String email) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.del(pendingUserKey(email));
        }
    }

    
    public static void cacheProduct(Product product) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.setex(productKey(product.getId()),
                    RedisConnect.getTtlProduct(),
                    GSON.toJson(product));
        }
    }

    
    public static Product getCachedProduct(int id) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            String json = jedis.get(productKey(id));
            return json != null ? GSON.fromJson(json, Product.class) : null;
        }
    }

    
    public static void cacheProductList(String suffix, List<Product> products) {
        Type listType = new TypeToken<List<Product>>() {}.getType();
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.setex(productListKey(suffix),
                    RedisConnect.getTtlProduct(),
                    GSON.toJson(products, listType));
        }
    }

    
    public static List<Product> getCachedProductList(String suffix) {
        Type listType = new TypeToken<List<Product>>() {}.getType();
        try (Jedis jedis = RedisConnect.getConnection()) {
            String json = jedis.get(productListKey(suffix));
            return json != null ? GSON.fromJson(json, listType) : null;
        }
    }

    
    public static void evictProduct(int id) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.del(productKey(id));
        }
    }

    
    public static void evictProductList(String suffix) {
        try (Jedis jedis = RedisConnect.getConnection()) {
            jedis.del(productListKey(suffix));
        }
    }
}
