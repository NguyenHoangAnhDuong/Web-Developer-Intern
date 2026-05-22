package vn.edu.hcmuaf.fit.ttltw.config;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class VnPayConfig {
    private static final String DEFAULT_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream in = VnPayConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) {
        }
        return p;
    }

    public static String createPaymentUrl(int orderId, double amount, HttpServletRequest request) throws Exception {
        Properties props = loadProps();
        String vnp_PayUrl = props.getProperty("vnpay.url", DEFAULT_PAY_URL);
        String vnp_TmnCode = props.getProperty("vnpay.tmncode", "");
        String vnp_HashSecret = props.getProperty("vnpay.hashsecret", "");
        String configuredReturn = props.getProperty("vnpay.return_url", "").trim();

        String vnp_ReturnUrl;
        if (!configuredReturn.isEmpty()) {
            vnp_ReturnUrl = configuredReturn;
        } else {
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            String ctx = request.getContextPath();
            vnp_ReturnUrl = scheme + "://" + host + (port == 80 || port == 443 ? "" : ":" + port) + ctx + "/vnpay-return";
        }

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", orderId + "_" + System.currentTimeMillis());

        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);

        vnp_Params.put("vnp_OrderType", "other");

        vnp_Params.put("vnp_Locale", "vn");

        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);

        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        //Sau 15 phút  user không thanh toán được nữa VNPAY báo giao dịch hết hạn
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        String vnp_CreateDate = formatter.format(cld.getTime());

        cld.add(Calendar.MINUTE, 15);

        String vnp_ExpireDate = formatter.format(cld.getTime());

        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                String encName = java.net.URLEncoder.encode(fieldName, StandardCharsets.UTF_8.name());
                String encValue = java.net.URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.name());
                hashData.append(fieldName).append('=').append(encValue).append('&');
                query.append(encName).append('=').append(encValue).append('&');
            }
        }
        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
        if (query.length() > 0) query.setLength(query.length() - 1);
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        return vnp_PayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    public static String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        hmac512.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : result) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
// xác minh dữ liệu trả về từ VNPAY có thật hay giả
    public static boolean validateSecureHash(Map<String, String> params) {
        try {
            Properties props = loadProps();
            String vnp_HashSecret = props.getProperty("vnpay.hashsecret", "");
            String vnp_SecureHash = params.get("vnp_SecureHash");
            if (vnp_SecureHash == null || vnp_SecureHash.isBlank()) return false;

            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            for (String fieldName : fieldNames) {
                if ("vnp_SecureHash".equals(fieldName) || "vnp_SecureHashType".equals(fieldName)) continue;
                String fieldValue = params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    String encValue = java.net.URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.name());
                    hashData.append(fieldName).append('=').append(encValue).append('&');
                }
            }
            if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
            String calcHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            return calcHash.equalsIgnoreCase(vnp_SecureHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
