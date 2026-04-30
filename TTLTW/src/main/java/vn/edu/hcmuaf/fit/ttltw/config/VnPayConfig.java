package vn.edu.hcmuaf.fit.ttltw.config;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VnPayConfig {
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:8080/WebDynamic/vnpay-return";
    public static String vnp_TmnCode = "2QX1S61S"; // Lấy từ mail VNPay Sandbox
    public static String vnp_HashSecret = "99Z6E6Z6Z6Z6Z6Z6Z6Z6Z6Z6Z6Z6Z6Z6"; // Lấy từ mail VNPay Sandbox

    public static String createPaymentUrl(int orderId, double amount, HttpServletRequest request) throws Exception {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long)amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId + "_" + System.currentTimeMillis());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + orderId);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", request.getRemoteAddr());
        vnp_Params.put("vnp_CreateDate", new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                hashData.append(fieldName).append('=').append(java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString())).append('&');
                query.append(java.net.URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString())).append('&');
            }
        }
        hashData.setLength(hashData.length() - 1);
        query.setLength(query.length() - 1);
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        return vnp_PayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    public static String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        hmac512.init(new SecretKeySpec(key.getBytes(), "HmacSHA512"));
        byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : result) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
}
