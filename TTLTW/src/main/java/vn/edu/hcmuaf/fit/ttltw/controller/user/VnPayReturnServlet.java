package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.config.VnPayConfig;
import vn.edu.hcmuaf.fit.ttltw.service.OrderService;
import vn.edu.hcmuaf.fit.ttltw.service.ShippingService;
import vn.edu.hcmuaf.fit.ttltw.service.SuperAIService;

@WebServlet("/vnpay-return")
public class VnPayReturnServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final SuperAIService superAI = new SuperAIService();
    private final ShippingService shippingService = new ShippingService();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) params.put(k, v[0]);
        });
        if (!VnPayConfig.validateSecureHash(params)) {
            resp.sendRedirect("cart?action=checkout&error=payment_invalid_signature");
            return;
        }

        String code = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        int orderId = Integer.parseInt(txnRef.split("_")[0]);

        if ("00".equals(code)) {
            // Lấy thông tin đơn hàng để cập nhật đúng trạng thái
            var orderOpt = orderService.getOrderById(orderId);
            
            if (orderOpt.isEmpty()) {
                resp.sendRedirect("user/order-detail?orderId=" + orderId + "&payment=error&msg=Order not found");
                return;
            }
            
            var order = orderOpt.get();
            int currentStatus = order.getStatus();
            
            // Chuyển từ status 1   sang 2
            // đẩy đơn sang vận chuyển
            if ( currentStatus == 1  ) {
                orderService.updateStatusOnly(orderId, 2);
                try {
                    var address = orderService.getDefaultAddress(order.getUserId());
                    if (address != null) {
                        String tracking = superAI.createRealOrder(
                                orderId,
                                address.getName(),
                                address.getPhoneNumber(),
                                address.getAddress(),
                                0,
                                order.getTotalAmount()
                        );
                        if (tracking != null && !tracking.isBlank()) {
                            shippingService.updateTrackingInfo(orderId, tracking,
                                order.getPartnerName() != null && !order.getPartnerName().isBlank()
                                    ? order.getPartnerName()
                                    : "SuperAI");
                            System.out.println("tracking saved: " + tracking);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("VnPayReturn: Lỗi gọi SuperAI - " + e.getMessage());
                    e.printStackTrace();
                }
            }
            resp.sendRedirect("user/order-detail?orderId=" + orderId + "&payment=success");
        } else {
            resp.sendRedirect("cart?action=checkout&error=payment_failed");
        }
    }
}
