package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.OrderService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/orders")
public class OrderAdminServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() {
        orderService = new OrderService();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String getOrderDetails = req.getParameter("getOrderDetails");
        if ("true".equals(getOrderDetails)) {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            Map<String, Object> orderDetails = orderService.getOrderDetailsForAdmin(orderId);
            resp.setContentType("application/json;charset=UTF-8");
            Gson gson = new Gson();
            resp.getWriter().print(gson.toJson(orderDetails));
            return;
        }
        String keyword = req.getParameter("keyword");
        String statusFilterRaw = req.getParameter("statusFilter");
        // String ajax = req.getParameter("ajax");

        Integer statusFilter = null;
        if (statusFilterRaw != null && !statusFilterRaw.isEmpty()) {
            statusFilter = Integer.parseInt(statusFilterRaw);
        }

        List<Map<String, Object>>  orders =
                (keyword != null && !keyword.isEmpty()) || statusFilter != null
                        ? orderService.searchForAdmin(keyword, statusFilter)
                        : orderService.getAllForAdmin();

        req.setAttribute("orders", orders);

        req.getRequestDispatcher("/views/admin/orderAdmin.jsp")
                .forward(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int orderId = Integer.parseInt(req.getParameter("orderId"));
        String action = req.getParameter("action");
        resp.setContentType("application/json;charset=UTF-8");
        if ("cancelOrder".equals(action)) {
            // Hủy đơn hàng với lý do
            String cancellationReason = req.getParameter("cancellationReason");
            Map<String, Object> result = orderService.cancelOrderWithReason(orderId, cancellationReason);
            boolean success = (boolean) result.get("success");
            String message = (String) result.get("message");
            if (!success) {
                System.err.println("Failed to cancel order " + orderId + ": " + message);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            resp.getWriter().print(String.format(
                    "{\"success\": %s, \"message\": \"%s\"}",
                    success,
                    message.replace("\"", "\\\"")
            ));
        } else {
            // Thay đổi trạng thái đơn hàng
            int newStatus = Integer.parseInt(req.getParameter("status"));
            Map<String, Object> result = orderService.updateStatus(orderId, newStatus);

            boolean success = (boolean) result.get("success");
            String message = (String) result.get("message");

            if (!success) {
                System.err.println("Failed to update order " + orderId + ": " + message);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            resp.getWriter().print(String.format(
                    "{\"success\": %s, \"message\": \"%s\"}",
                    success,
                    message.replace("\"", "\\\"")
            ));
        }
    }
}



