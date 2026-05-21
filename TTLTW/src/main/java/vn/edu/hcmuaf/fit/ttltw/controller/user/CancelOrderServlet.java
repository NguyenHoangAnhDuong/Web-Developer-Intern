package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.OrderService;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/order/cancel")
public class CancelOrderServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String orderIdStr =  request.getParameter("orderId") ;
            int orderId = Integer.parseInt(orderIdStr);

            // Gọi OrderService.updateStatus để hủy cả API và DB
            Map<String, Object> result = orderService.updateStatus(orderId, 4);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            if ((Boolean) result.get("success")) {
                response.getWriter().write("{\"status\":\"success\"}");
            } else {
                response.setStatus(400);
                response.getWriter().write("{\"status\":\"fail\", \"message\":\"" + result.get("message") + "\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("{\"status\":\"error\"}");
        }
    }
}
