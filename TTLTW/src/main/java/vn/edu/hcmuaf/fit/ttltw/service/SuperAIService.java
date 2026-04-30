package vn.edu.hcmuaf.fit.ttltw.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SuperAIService {
    private static String API_TOKEN;
    private static String API_URL;
    private static boolean MOCK_MODE = false;

    static {
        try (InputStream input = SuperAIService.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                API_TOKEN = prop.getProperty("supership.api.token");
                API_URL = prop.getProperty("supership.api.url");
                String mockProp = prop.getProperty("supership.mock.mode", "false");
                MOCK_MODE = Boolean.parseBoolean(mockProp);
                // debug
                System.out.println(" loaded config - URL: " + API_URL);
                System.out.println(" token: " + (API_TOKEN != null ? API_TOKEN.substring(0, Math.min(10, API_TOKEN.length())) + "..." : "NULL"));
                System.out.println(" mock Mode: " + MOCK_MODE + (MOCK_MODE ? " fake tracking number" : ""));

                // kiểm tra kết nối mạng
                if (!MOCK_MODE && API_URL != null) {
                    try {
                        String host = URI.create(API_URL).getHost();
                        java.net.InetAddress.getByName(host);
                        System.out.println("network test PASSED - Can reach: " + host);
                    } catch (Exception netEx) {
                        System.err.println("network test FAILED - Cannot reach: " + API_URL);
                        System.err.println("   Reason: " + netEx.getMessage());
                    }
                }


            }
        } catch (Exception e) {
            System.err.println(" superAIServices Lỗi tải cấu hình: " + e.getMessage());
            e.printStackTrace();
            MOCK_MODE = true;
        }
    }
// gọi API bên thứ 3
    public String createRealOrder(int orderId, String name, String phone, String fullAddress, double amount) {
        // fake tracking number phòng trường hợp mạng lỗi , hoặc API bên thứ 3 gặp lỗi
        if (MOCK_MODE) {
            String mockTracking = "SHIP" + System.currentTimeMillis() + "-MOCK";
            return mockTracking;
        }

        try {
            // Validate config
            if (API_URL == null || API_URL.trim().isEmpty()) {
                return null;
            }
            if (API_TOKEN == null || API_TOKEN.trim().isEmpty()) {
                return null;
            }
            String[] parsed = parseAddress(fullAddress);
            //  xây dựng cấu trúc JSON theo mẫu curl của SuperAI
            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("phone", phone);
            body.put("address", parsed[0]);  // street
            body.put("commune", parsed[1]);  // xã
            body.put("district", parsed[2]); // huyện
            body.put("province", parsed[3]); // tỉnh

            body.put("amount", (long) amount);
            body.put("value", (long) amount);
            body.put("weight", 200);
            body.put("payer", "1");
            body.put("config", "1");
            body.put("soc", "DH" + orderId);
            body.put("product_type", "2");
            body.put("note", "Cho xem hàng, không thử");

            // thêm mảng products
            List<Map<String, Object>> products = new ArrayList<>();
            Map<String, Object> p = new HashMap<>();
            p.put("sku", "DH" + orderId);
            p.put("name", "Đơn hàng #" + orderId);
            p.put("price", (long) amount);
            p.put("quantity", 1);
            p.put("weight", 200);
            products.add(p);
            body.put("products", products);

            String jsonBody = new Gson().toJson(body);

            // Log kiểm tra dữ liệu trước khi gửi
            System.out.println("  request to: " + API_URL);
            System.out.println("  Request JSON: " + jsonBody);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Token", API_TOKEN)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            try {
                java.net.InetAddress.getByName(URI.create(API_URL).getHost());
            } catch (Exception dnsEx) {
                return null;
            }

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String responseBody = response.body();

            System.out.println("response Status: " + response.statusCode());
            System.out.println("response Body: " + responseBody);

            if (response.statusCode() != 200) {
                System.err.println(" API Error - HTTP " + response.statusCode());
                System.err.println("error Response: " + responseBody);
                return null;
            }

            JsonObject resJson = JsonParser.parseString(responseBody).getAsJsonObject();

            // Check for error
            if (resJson.has("error") && resJson.get("error").getAsBoolean()) {
                String errorMsg = resJson.has("message") ? resJson.get("message").getAsString() : "Unknown error";
                System.err.println("  API trả về error: " + errorMsg);
                return null;
            }

            String trackingCode = null;

            if (resJson.has("data")) {
                JsonObject data = resJson.getAsJsonObject("data");
                if (data.has("superai_code") && !data.get("superai_code").isJsonNull()) {
                    trackingCode = data.get("superai_code").getAsString();
                }
            }else if (resJson.has("results")) {
                JsonObject results = resJson.getAsJsonObject("results");
                if (results.has("code")) {
                    trackingCode = results.get("code").getAsString();
                }
            }

            if (trackingCode != null && !trackingCode.trim().isEmpty()) {
                System.out.println("tracking number: " + trackingCode);
                return trackingCode;
            } else {
                System.err.println("no tracking number in response");
                System.err.println("response : " + resJson.toString());
                return null;
            }

        } catch (Exception e) {
            System.err.println("  type: " + e.getClass().getName());
            System.err.println("   message: " + (e.getMessage() != null ? e.getMessage() : "NULL"));
            e.printStackTrace();
            return null;
        }
    }
// chuyển đổi địa chỉ để lấy địa chỉ chính xác không bị lỗi khi tách json
    private String[] parseAddress(String fullAddress) {
        if (fullAddress == null) return new String[]{"", "", "", ""};
        // →xử lí 2 trường hợp dạng cách nhau dấu '/' và ','
        String normalized = fullAddress
                .replace("/ \r\n", "|||")
                .replace("/\r\n", "|||")
                .replace("/ ", "|||")
                .replace("/", "|||")
                .replace(",", "|||");

        String[] parts = normalized.split("\\|\\|\\|");

        // Clean up
        List<String> cleaned = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }
        String street = cleaned.size() > 0 ? cleaned.get(0) : "";
        String commune = cleaned.size() > 1 ? cleaned.get(1) : "";
        String district = cleaned.size() > 2 ? cleaned.get(2) : "";
        String province = cleaned.size() > 3 ? cleaned.get(3) : "";

        return new String[]{street, commune, district, province};
    }

    // gọi API hủy đơn hàng
    public boolean cancelOrder(String trackingCode) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("code", trackingCode);
            String json = new Gson().toJson(body);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.superai.vn/v1/platform/orders/cancel"))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Token", API_TOKEN) //
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}