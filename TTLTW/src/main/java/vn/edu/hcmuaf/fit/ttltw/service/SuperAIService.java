package vn.edu.hcmuaf.fit.ttltw.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import vn.edu.hcmuaf.fit.ttltw.model.ShippingZoneFees;

import java.io.InputStream;
import java.math.BigDecimal;
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
    private static String CANCEL_URL;
    private static String CANCEL_API_TOKEN;
    private static String CARRIER_LIST_URL;
    private static String CARRIER_API_TOKEN;
    private static String SHIPPING_FEES_URL;
    private static String SHIPPING_FEES_API_TOKEN;
    private static String SENDER_PROVINCE;
    private static String SENDER_DISTRICT;
    private static String SENDER_COMMUNE;
    private static String SENDER_ADDRESS;
    private static long DEFAULT_WEIGHT = 500L;
    private static long DEFAULT_VALUE = 0L;
    private static boolean MOCK_MODE = false;

    static {
        try (InputStream input = SuperAIService.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                API_TOKEN = prop.getProperty("supership.api.token");
                API_URL = prop.getProperty("supership.api.url");
                CANCEL_URL = prop.getProperty("supership.cancel.api.url", "https://api.superai.vn/v1/platform/orders/cancel");
                CANCEL_API_TOKEN = prop.getProperty("supership.cancel.api.token", API_TOKEN);
                // Load new optional endpoints safely
                try {
                    CARRIER_LIST_URL = prop.getProperty("supership.carriers.api.url", "https://dev.superai.vn/v1/platform/carriers/list");
                    CARRIER_API_TOKEN = prop.getProperty("supership.carriers.api.token", API_TOKEN);
                    SHIPPING_FEES_URL = prop.getProperty("supership.shipping.fees.api.url", "https://dev.superai.vn/v1/platform/orders/price");
                    SHIPPING_FEES_API_TOKEN = prop.getProperty("supership.shipping.fees.api.token", API_TOKEN);
                    SENDER_PROVINCE = prop.getProperty("supership.sender.province", "");
                    SENDER_DISTRICT = prop.getProperty("supership.sender.district", "");
                    SENDER_COMMUNE = prop.getProperty("supership.sender.commune", "");
                    SENDER_ADDRESS = prop.getProperty("supership.sender.address", "");
                    String weightStr = prop.getProperty("supership.default.weight", "500");
                    String valueStr = prop.getProperty("supership.default.value", "0");
                    DEFAULT_WEIGHT = Long.parseLong(weightStr);
                    DEFAULT_VALUE = Long.parseLong(valueStr);
                } catch (Exception cfgEx) {
                    System.err.println(" Lỗi load shipping config: " + cfgEx.getMessage());
                    CANCEL_URL = "https://api.superai.vn/v1/platform/orders/cancel";
                    CARRIER_LIST_URL = "https://dev.superai.vn/v1/platform/carriers/list";
                    CARRIER_API_TOKEN = API_TOKEN;
                    SHIPPING_FEES_URL = "https://dev.superai.vn/v1/platform/orders/price";
                    SHIPPING_FEES_API_TOKEN = API_TOKEN;
                    SENDER_PROVINCE = "";
                    SENDER_DISTRICT = "";
                    SENDER_COMMUNE = "";
                    SENDER_ADDRESS = "";
                    DEFAULT_WEIGHT = 500L;
                    DEFAULT_VALUE = 0L;
                }
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

    public String[] parseAddressPublic(String fullAddress) {
        return parseAddress(fullAddress);
    }

    public List<Map<String, Object>> getShippingCarriers() {
        List<Map<String, Object>> carriers = new ArrayList<>();
        System.out.println("[SuperAI] Calling carriers API: " + CARRIER_LIST_URL);
        JsonObject root = fetchJsonFromUrl(CARRIER_LIST_URL);
        if (root == null) {
            System.err.println("[SuperAI] carriers API returned null response");
            return carriers;
        }
        JsonArray carrierArray = extractArray(root, "data");
        if (carrierArray == null) {
            System.err.println("[SuperAI] carriers API missing data array. Raw response: " + root);
            return carriers;
        }

        for (JsonElement element : carrierArray) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject item = element.getAsJsonObject();
            Map<String, Object> carrier = new HashMap<>();
            if (item.has("id") && !item.get("id").isJsonNull()) {
                carrier.put("id", item.get("id").getAsInt());
            }
            if (item.has("name") && !item.get("name").isJsonNull()) {
                carrier.put("name", item.get("name").getAsString());
            }
            if (item.has("code") && !item.get("code").isJsonNull()) {
                carrier.put("code", item.get("code").getAsString());
            }
            carriers.add(carrier);
        }

        return carriers;
    }

    public List<ShippingZoneFees> getShippingFeeOptions() {
        return getShippingFeeOptions("", DEFAULT_WEIGHT, DEFAULT_VALUE);
    }

    public List<ShippingZoneFees> getShippingFeeOptions(String fullAddress, long weight, long value) {
        List<ShippingZoneFees> options = new ArrayList<>();
        String priceRequestBody = buildPriceRequestBody(fullAddress, weight, value);
        System.out.println("[SuperAI] Calling price API: " + SHIPPING_FEES_URL);
        System.out.println("[SuperAI] Sender config: province=" + SENDER_PROVINCE + ", district=" + SENDER_DISTRICT + ", commune=" + SENDER_COMMUNE + ", address=" + SENDER_ADDRESS);
        System.out.println("[SuperAI] price request body: " + priceRequestBody);

        JsonObject root = fetchJsonFromUrl(SHIPPING_FEES_URL, priceRequestBody);
        if (root == null) {
            System.err.println("[SuperAI] price API returned null response");
            return options;
        }

        System.out.println("[SuperAI] price API full response: " + root.toString());

        JsonArray serviceArray = null;
        if (root.has("data") && root.get("data").isJsonObject()) {
            JsonObject data = root.getAsJsonObject("data");
            System.out.println("[SuperAI] data object: " + data.toString());
            if (data.has("services") && data.get("services").isJsonArray()) {
                serviceArray = data.getAsJsonArray("services");
            }
        } else if (root.has("data") && root.get("data").isJsonArray()) {
            serviceArray = root.getAsJsonArray("data");
        }

        if (serviceArray == null) {
            System.err.println("[SuperAI] price API missing services array. Raw response: " + root);
            return options;
        }
        System.out.println("[SuperAI] Found " + serviceArray.size() + " services in price API response");

        for (JsonElement element : serviceArray) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject item = element.getAsJsonObject();
            ShippingZoneFees fee = new ShippingZoneFees();
            if (item.has("carrier_id") && !item.get("carrier_id").isJsonNull()) {
                fee.setId(item.get("carrier_id").getAsInt());
                fee.setZoneId(item.get("carrier_id").getAsInt());
            }
            if (item.has("carrier_name") && !item.get("carrier_name").isJsonNull()) {
                fee.setShippingMethod(item.get("carrier_name").getAsString());
            }
            if (item.has("fee") && !item.get("fee").isJsonNull()) {
                fee.setBaseFee(item.get("fee").getAsBigDecimal());
            } else if (item.has("shipment_fee") && !item.get("shipment_fee").isJsonNull()) {
                fee.setBaseFee(item.get("shipment_fee").getAsBigDecimal());
            }
            if (item.has("insurance_fee") && !item.get("insurance_fee").isJsonNull()) {
                fee.setPerKgFee(item.get("insurance_fee").getAsBigDecimal());
            }
            if (item.has("estimated_delivery") && !item.get("estimated_delivery").isJsonNull()) {
                fee.setEstimatedDays(item.get("estimated_delivery").getAsString());
            }
            fee.setIsActive(1);
            options.add(fee);
        }
        
        // Fallback: if no services found, create default option so UI shows at least one fee option
        if (options.isEmpty()) {
            System.err.println("[SuperAI] WARNING: price API returned 0 services. Creating fallback default option with base fee 30000");
            ShippingZoneFees defaultFee = new ShippingZoneFees();
            defaultFee.setId(0);
            defaultFee.setZoneId(0);
            defaultFee.setShippingMethod("Vận chuyển tiêu chuẩn (chưa cập nhật phí)");
            defaultFee.setBaseFee(new BigDecimal("30000"));
            defaultFee.setPerKgFee(BigDecimal.ZERO);
            defaultFee.setEstimatedDays("3-5 ngày");
            defaultFee.setIsActive(1);
            options.add(defaultFee);
        }

        return options;
    }

    public List<ShippingZoneFees> getShippingServices(String fullAddress, long weight, long value) {
        return getShippingFeeOptions(fullAddress, weight > 0 ? weight : DEFAULT_WEIGHT, value > 0 ? value : DEFAULT_VALUE);
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
            System.out.println("[SuperAI] Calling cancel API: " + CANCEL_URL + " with code=" + trackingCode);
            System.out.println("[SuperAI] cancel request body: " + json);

            JsonObject response = fetchJsonFromUrl(CANCEL_URL, json);
            if (response == null) {
                System.err.println("[SuperAI] cancel API returned null response for code=" + trackingCode);
                return false;
            }

            System.out.println("[SuperAI] cancel response body: " + response);

            if (response.has("error") && response.get("error").isJsonPrimitive()) {
                boolean ok = !response.get("error").getAsBoolean();
                if (!ok) {
                    String message = response.has("message") && !response.get("message").isJsonNull()
                            ? response.get("message").getAsString()
                            : "Unknown cancel error";
                    System.err.println("[SuperAI] cancel API reported error. message=" + message + ", code=" + trackingCode);
                }
                return ok;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private JsonObject fetchJsonFromUrl(String url) {
        return fetchJsonFromUrl(url, null);
    }

    private JsonObject fetchJsonFromUrl(String url, String requestBody) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30));

            String endpointToken = resolveTokenByUrl(url);
            if (endpointToken != null && !endpointToken.trim().isEmpty()) {
                requestBuilder.header("Token", endpointToken);
            } else {
                System.err.println("[SuperAI] Missing token for endpoint: " + url);
            }

            if (requestBody == null) {
                requestBuilder.GET();
            } else {
                requestBuilder.header("Content-Type", "application/json; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));
            }

            String method = requestBody == null ? "GET" : "POST";

            HttpResponse<String> response = client.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            String responseBody = response.body();
            System.out.println("[SuperAI] " + method + " " + url + " -> HTTP " + response.statusCode());

            if (response.statusCode() != 200) {
                System.err.println("[SuperAI] " + method + " API Error - HTTP " + response.statusCode() + " for " + url);
                System.err.println("[SuperAI] error response body: " + responseBody);
                return null;
            }

            return JsonParser.parseString(responseBody).getAsJsonObject();
        } catch (Exception e) {
            String method = requestBody == null ? "GET" : "POST";
            System.err.println("[SuperAI] " + method + " API failed for " + url + ": " + e.getMessage());
            return null;
        }
    }

    private String buildPriceRequestBody(String fullAddress, long weight, long value) {
        String[] parsed = parseAddress(fullAddress);
        Map<String, Object> body = new HashMap<>();
        body.put("sender_province", SENDER_PROVINCE);
        body.put("sender_district", SENDER_DISTRICT);
        body.put("sender_commune", SENDER_COMMUNE);
        body.put("sender_address", SENDER_ADDRESS);
        body.put("receiver_province", parsed[3]);
        body.put("receiver_district", parsed[2]);
        body.put("receiver_commune", parsed[1]);
        body.put("receiver_address", parsed[0]);
        body.put("weight", weight > 0 ? weight : DEFAULT_WEIGHT);
        body.put("value", value > 0 ? value : DEFAULT_VALUE);
        return new Gson().toJson(body);
    }

    private String resolveTokenByUrl(String url) {
        if (url == null) {
            return fallbackToken(API_TOKEN);
        }

        if (SHIPPING_FEES_URL != null && url.equals(SHIPPING_FEES_URL)) {
            return fallbackToken(SHIPPING_FEES_API_TOKEN);
        }

        if (CARRIER_LIST_URL != null && url.equals(CARRIER_LIST_URL)) {
            return fallbackToken(CARRIER_API_TOKEN);
        }

        if (CANCEL_URL != null && url.equals(CANCEL_URL)) {
            return fallbackToken(CANCEL_API_TOKEN);
        }

        return fallbackToken(API_TOKEN);
    }

    private String fallbackToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            return token;
        }
        if (API_TOKEN != null && !API_TOKEN.trim().isEmpty()) {
            return API_TOKEN;
        }
        return null;
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private JsonArray extractArray(JsonObject root, String fieldName) {
        if (root == null || fieldName == null || fieldName.isBlank() || !root.has(fieldName) || root.get(fieldName).isJsonNull()) {
            return null;
        }

        if (root.get(fieldName).isJsonArray()) {
            return root.getAsJsonArray(fieldName);
        }

        if (root.get(fieldName).isJsonObject()) {
            JsonObject nested = root.getAsJsonObject(fieldName);
            if (nested.has("services") && nested.get("services").isJsonArray()) {
                return nested.getAsJsonArray("services");
            }
        }

        return null;
    }
}