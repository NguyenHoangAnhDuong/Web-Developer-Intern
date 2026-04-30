package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.OrderService;
import vn.edu.hcmuaf.fit.ttltw.service.SuperAIService;

import java.io.IOException;

@WebServlet("/api/order/cancel")
public class CancelOrderServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final SuperAIService superAIService = new SuperAIService();


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String orderIdStr =  request.getParameter("orderId") ;
            int orderId = Integer.parseInt(orderIdStr);
            String trackingCode = orderService.getTrackingByOrderId(orderId);

            if (trackingCode == null || trackingCode.isEmpty()) {
                response.setStatus(400);
                response.getWriter().write("{\"error\":\"Không có mã vận đơn\"}");
                return;
            }


            boolean success =superAIService.cancelOrder(trackingCode);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            if (success) {
                response.getWriter().write("{\"status\":\"success\"}");
            } else {
                response.setStatus(400);
                response.getWriter().write("{\"status\":\"fail\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
        }



    }
}
