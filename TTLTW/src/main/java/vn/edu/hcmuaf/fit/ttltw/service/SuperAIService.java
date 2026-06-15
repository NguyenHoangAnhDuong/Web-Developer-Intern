package vn.edu.hcmuaf.fit.ttltw.service;

import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import vn.edu.hcmuaf.fit.ttltw.model.ShippingZoneFees;

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
    private String lastCancelErrorMessage = null;

    static {
        try (InputStream input = SuperAIService.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(new InputStreamReader(input, StandardCharsets.UTF_8));
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


            }
        } catch (Exception e) {
            System.err.println("superAIServices lỗi tải cấu hình: " + e.getMessage());
            MOCK_MODE = true;
        }
    }



    // lấy danh sách đơn vị vận chuyển từ superAI
    public List<Map<String, Object>> getShippingCarriers() {
        List<Map<String, Object>> carriers = new ArrayList<>();
        JsonObject root = fetchJsonFromUrl(CARRIER_LIST_URL);
        if (root == null) {
            System.err.println("  API phản hồi null");
            return carriers;
        }
        JsonArray carrierArray = extractArray(root, "data");
        if (carrierArray == null) {
            System.err.println(" carrierArray == null  " + root);
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


    // Gọi API giá vận chuyển
    public List<ShippingZoneFees> getShippingFeeOptions(String fullAddress, long weight, long value) {
        List<ShippingZoneFees> options = new ArrayList<>();
        String priceRequestBody = buildPriceRequestBody(fullAddress, weight, value);

        JsonObject root = fetchJsonFromUrl(SHIPPING_FEES_URL, priceRequestBody);
        if (root == null) {
            System.err.println("giá phản hồi null");
            return options;
        }

        JsonArray serviceArray = null;
        if (root.has("data") && root.get("data").isJsonObject()) {
            JsonObject data = root.getAsJsonObject("data");
            if (data.has("services") && data.get("services").isJsonArray()) {
                serviceArray = data.getAsJsonArray("services");
            }
        } else if (root.has("data") && root.get("data").isJsonArray()) {
            serviceArray = root.getAsJsonArray("data");
        }

        if (serviceArray == null) {
            System.err.println("serviceArray == null" + root);
            return options;
        }

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

        if (options.isEmpty()) {
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

// Tạo một đơn hàng
    public String createRealOrder(int orderId, String name, String phone, String fullAddress, double amount, double orderValue) {
        if (MOCK_MODE) {
            String mockTracking = "SHIP" + System.currentTimeMillis() + "-MOCK";
            return mockTracking;
        }

        try {
            if (API_URL == null || API_URL.trim().isEmpty()) {
                return null;
            }
            if (API_TOKEN == null || API_TOKEN.trim().isEmpty()) {
                return null;
            }
            String[] parsed = parseAddress(fullAddress);
            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("phone", phone);
            body.put("address", parsed[0]);  // street
            body.put("commune", parsed[1]);  // xã
            body.put("district", parsed[2]); // huyện
            body.put("province", parsed[3]); // tỉnh

            body.put("amount", (long) orderValue);
            body.put("value", (long) amount);
            body.put("weight", 200);
            body.put("payer", "1");
            body.put("config", "1");
            body.put("soc", "DH" + orderId);
            body.put("product_type", "2");
            body.put("note", "Cho xem hàng, không thử");

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



            if (response.statusCode() != 200) {
                System.err.println(response.statusCode());
                System.err.println( responseBody);
                return null;
            }

            JsonObject resJson = JsonParser.parseString(responseBody).getAsJsonObject();

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
                return trackingCode;
            } else {
                System.err.println( "tracking number k phan hồi");
                return null;
            }

        } catch (Exception e) {
            System.err.println("createRealOrder failed: " + e.getClass().getName() + " - " + (e.getMessage() != null ? e.getMessage() : "NULL"));
            return null;
        }
    }
    //  chia đường, xã, quận ,tỉnh.
    private String[] parseAddress(String fullAddress) {
        if (fullAddress == null) return new String[]{"", "", "", ""};
        String normalized = fullAddress
                .replace("/ \r\n", "|||")
                .replace("/\r\n", "|||")
                .replace("/ ", "|||")
                .replace("/", "|||")
                .replace(",", "|||");

        String[] parts = normalized.split("\\|\\|\\|");

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

    // hủy đơn hàng
    public boolean cancelOrder(String trackingCode) {
        lastCancelErrorMessage = null;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("code", trackingCode);
            String json = new Gson().toJson(body);
            JsonObject response = fetchJsonFromUrl(CANCEL_URL, json);
            if (response != null) {
                System.out.println( trackingCode + " -> " + response.toString());
            }

            if (response == null) {
                lastCancelErrorMessage = "API hủy đơn hàng không phản hồi";
                System.err.println( trackingCode);
                return false;
            }

            String rawResponse = response.toString();

            if (response.has("error") && response.get("error").isJsonPrimitive()) {
                boolean ok = !response.get("error").getAsBoolean();
                if (!ok) {
                    String message = response.has("message") && !response.get("message").isJsonNull()
                            ? response.get("message").getAsString()
                            : "Unknown cancel error";

                    String carrierCode = null;
                    if (response.has("code") && !response.get("code").isJsonNull()) {
                        try { carrierCode = response.get("code").getAsString(); } catch (Exception ignored) {}
                    }
                    if (carrierCode == null && response.has("data") && response.get("data").isJsonObject()) {
                        JsonObject data = response.getAsJsonObject("data");
                        if (data.has("code") && !data.get("code").isJsonNull()) {
                            try { carrierCode = data.get("code").getAsString(); } catch (Exception ignored) {}
                        }
                    }

                    String composed = message + (carrierCode != null ?   " "+ carrierCode   : " | raw:" + rawResponse);
                    lastCancelErrorMessage = composed;
                    System.err.println( composed + ", " + trackingCode);
                }
                return ok;
            }

            if (response.has("message") && !response.get("message").isJsonNull()) {
                String msg = response.get("message").getAsString();
                if (msg != null && !msg.trim().isEmpty()) {
                    lastCancelErrorMessage = msg + " | raw:" + rawResponse;
                }
            }

            return true;

        } catch (Exception e) {
            lastCancelErrorMessage = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }
    public String getLastCancelErrorMessage() {
        return lastCancelErrorMessage;
    }

    // gửi yêu cầu Get
    private JsonObject fetchJsonFromUrl(String url) {
        return fetchJsonFromUrl(url, null);
    }

    // gửi  GET hoặc POST và  phản hồi JSON
    private JsonObject fetchJsonFromUrl(String url, String requestBody) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            String endpointToken = resolveTokenByUrl(url);

            int maxAttempts = 3;
            int attempt = 0;
            while (attempt < maxAttempts) {
                attempt++;
                try {
                    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Accept", "application/json")
                            .header("User-Agent", "TTLTW-SuperAI-Client/1.0")
                            .timeout(Duration.ofSeconds(30));

                    if (endpointToken != null && !endpointToken.trim().isEmpty()) {
                        requestBuilder.header("Token", endpointToken);
                    } else {
                        System.err.println( url);
                    }

                    if (requestBody == null) {
                        requestBuilder.GET();
                    } else {
                        requestBuilder.header("Content-Type", "application/json; charset=UTF-8")
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));
                    }

                    HttpResponse<String> response = client.send(
                            requestBuilder.build(),
                            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
                    );

                    String responseBody = response.body();

                    if (response.statusCode() != 200) {
                        System.err.println( responseBody);
                        try {
                            return JsonParser.parseString(responseBody).getAsJsonObject();
                        } catch (Exception parseEx) {
                            System.err.println(parseEx.getMessage());
                        }
                    } else {
                        try {
                            return JsonParser.parseString(responseBody).getAsJsonObject();
                        } catch (Exception parseEx) {
                            System.err.println( parseEx.getMessage());
                            return null;
                        }
                    }

                } catch (Exception e) {
                    System.err.println( e.getMessage());
                    if (attempt >= maxAttempts) {
                        System.err.println( url);
                        return null;
                    }
                    try {
                        Thread.sleep(250L * attempt); // small backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println(  e.getMessage());
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

    // Trả về token đã được cấu hình, sử dụng token chính khi cần
    private String fallbackToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            return token;
        }
        if (API_TOKEN != null && !API_TOKEN.trim().isEmpty()) {
            return API_TOKEN;
        }
        return null;
    }


    // Trích xuất mảng

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