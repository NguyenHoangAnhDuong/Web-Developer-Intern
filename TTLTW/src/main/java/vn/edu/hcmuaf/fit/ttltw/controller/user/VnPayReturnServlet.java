package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.OrderService;
import vn.edu.hcmuaf.fit.ttltw.service.SuperAIService;

import java.io.IOException;

@WebServlet("/vnpay-return")
public class VnPayReturnServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final SuperAIService superAI = new SuperAIService();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("vnp_ResponseCode");
        String txnRef = req.getParameter("vnp_TxnRef");
        int orderId = Integer.parseInt(txnRef.split("_")[0]);

        if ("00".equals(code)) {
            // cập nhập trạng thái đã thanh toán
            orderService.updateStatus(orderId, 3);

            // đẩy đơn sang vận chuyển
            var orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                // giả lập thông tin hoặc lấy từ bảng Address
                superAI.createRealOrder(orderId, "Khách thanh toán Online", "090xxx", "Địa chỉ lấy từ DB", 0); // COD = 0 vì đã trả tiền
            }

            resp.sendRedirect("user/order-detail?orderId=" + orderId + "&payment=success");
        } else {
            resp.sendRedirect("cart?action=checkout&error=payment_failed");
        }
    }
}
