package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.*;
import vn.edu.hcmuaf.fit.ttltw.service.OrderDetailService;
import vn.edu.hcmuaf.fit.ttltw.service.OrderService;
import vn.edu.hcmuaf.fit.ttltw.utils.SidebarUtil;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "OrderUserServlet", urlPatterns = { "/user/order" })
public class OrderUserServlet extends HttpServlet {

    private OrderService orderService;
    private OrderDetailService orderDetailService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        orderDetailService = new OrderDetailService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Kiểm tra user đã login chưa
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User users = (User) session.getAttribute("user");
        int userId = users.getId();

        // Lấy action từ request (nếu có)
        String action = request.getParameter("action");

        if ("cancel".equals(action)) {
            // Xử lý hủy đơn hàng qua GET (nếu cần)
            handleCancelOrder(request, response, userId);
            return;
        }

        // Hiển thị danh sách đơn hàng
        displayOrders(request, response, userId);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Kiểm tra user đã login
        HttpSession session = request.getSession(false);
        User users = (User) session.getAttribute("user");

        if (users == null) {
            response.getWriter().print("{\"success\":false,\"message\":\"Vui lòng đăng nhập\"}");
            return;
        }
        int userId = users.getId();
        String action = request.getParameter("action");

        if ("cancel".equals(action)) {
            handleCancelOrderAjax(request, response, userId);
        } else if ("repurchase".equals(action)) {
            handleRepurchaseOrderAjax(request, response, userId);
        } else {
            response.getWriter().print("{\"success\":false,\"message\":\"Action không hợp lệ\"}");
        }
    }

    /**
     * Hiển thị danh sách đơn hàng
     */
    private void displayOrders(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {

        String statusParam = request.getParameter("status");
        List<Order> orders;

        // Lấy đơn hàng theo status hoặc tất cả
        if (statusParam != null && !statusParam.equals("all")) {
            try {
                int status = Integer.parseInt(statusParam);
                orders = orderService.getUserOrdersByStatus(userId, status);
            } catch (NumberFormatException e) {
                orders = orderService.getUserOrders(userId);
            }
        } else {
            orders = orderService.getUserOrders(userId);
        }

        // Format dữ liệu cho JSP
        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        List<java.util.Map<String, Object>> ordersData = new java.util.ArrayList<>();

        if (orders != null && !orders.isEmpty()) {
            for (Order order : orders) {
                java.util.Map<String, Object> orderMap = new java.util.HashMap<>();
                orderMap.put("order", order);
                orderMap.put("id", order.getId());
                orderMap.put("status", order.getStatus());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("formattedTotal", currencyFormat.format(order.getTotalAmount()));

                // Status info
                orderMap.put("statusName", OrderService.getStatusName(order.getStatus()));
                orderMap.put("statusIcon", OrderService.getStatusIcon(order.getStatus()));
                orderMap.put("statusClass", OrderService.getStatusClass(order.getStatus()));

                // Order details với product info
                List<OrderDetail> orderDetails = orderDetailService.getOrderDetailsWithProduct(order.getId());
                List<java.util.Map<String, Object>> itemsList = new java.util.ArrayList<>();

                if (orderDetails != null && !orderDetails.isEmpty()) {
                    for (OrderDetail detail : orderDetails) {
                        java.util.Map<String, String> productInfo = orderDetailService
                                .getProductInfoByVariantId(detail.getVariantId());
                        java.util.Map<String, Object> itemMap = new java.util.HashMap<>();
                        itemMap.put("detail", detail);
                        itemMap.put("quantity", detail.getQuantity());
                        itemMap.put("price", detail.getPrice());
                        itemMap.put("formattedPrice", currencyFormat.format(detail.getPrice()));
                        itemMap.put("productName", productInfo != null ? productInfo.get("productName") : "N/A");
                        itemMap.put("variantName", productInfo != null ? productInfo.get("variantName") : "");
                        itemMap.put("imagePath",
                                productInfo != null ? productInfo.get("imagePath") : "asset/img/product/default.jpg");
                        itemsList.add(itemMap);
                    }
                }
                orderMap.put("items", itemsList);
                ordersData.add(orderMap);
            }
        }

        // Đưa dữ liệu vào request
        request.setAttribute("ordersData", ordersData);
        request.setAttribute("currentStatus", statusParam);

        // Set sidebar data
        request.setAttribute("activeMenu", "order");
        SidebarUtil.setSidebarData(request);

        // Forward đến JSP
        request.getRequestDispatcher("/views/user/orderUser.jsp").forward(request, response);
    }
    private void handleCancelOrderAjax(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {

        try {
            String orderIdParam = request.getParameter("orderId");

            if (orderIdParam == null || orderIdParam.isEmpty()) {
                response.getWriter().print("{\"success\":false,\"message\":\"Thiếu thông tin đơn hàng\"}");
                return;
            }

            int orderId = Integer.parseInt(orderIdParam);

            // Kiểm tra đơn hàng có thuộc về user không
            if (!orderService.isOrderBelongToUser(orderId, userId)) {
                response.getWriter().print("{\"success\":false,\"message\":\"Không có quyền hủy đơn hàng này\"}");
                return;
            }

            // Hủy đơn hàng
            boolean success = orderService.cancelOrder(orderId, userId);

            if (success) {
                response.getWriter().print("{\"success\":true,\"message\":\"Hủy đơn hàng thành công\"}");
            } else {
                response.getWriter().print(
                        "{\"success\":false,\"message\":\"Không thể hủy đơn hàng. Chỉ có thể hủy đơn hàng đang ở trạng thái 'Đang lên đơn'\"}");
            }

        } catch (NumberFormatException e) {
            response.getWriter().print("{\"success\":false,\"message\":\"Mã đơn hàng không hợp lệ\"}");
        } catch (Exception e) {
            response.getWriter().print(
                    "{\"success\":false,\"message\":\"Có lỗi xảy ra: " + e.getMessage().replace("\"", "\\\"") + "\"}");
            e.printStackTrace();
        }
    }
    private void handleCancelOrder(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String orderIdParam = request.getParameter("orderId");

            if (orderIdParam != null && !orderIdParam.isEmpty()) {
                int orderId = Integer.parseInt(orderIdParam);

                if (orderService.isOrderBelongToUser(orderId, userId)) {
                    boolean success = orderService.cancelOrder(orderId, userId);

                    if (success) {
                        response.getWriter().print("{\"success\":true,\"message\":\"Hủy đơn hàng thành công\"}");
                    } else {
                        response.getWriter().print("{\"success\":false,\"message\":\"Không thể hủy đơn hàng\"}");
                    }
                } else {
                    response.getWriter().print("{\"success\":false,\"message\":\"Đơn hàng không thuộc về bạn\"}");
                }
            } else {
                response.getWriter().print("{\"success\":false,\"message\":\"Thiếu thông tin đơn hàng\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }
    private void handleRepurchaseOrderAjax(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {

        try {
            String orderIdParam = request.getParameter("orderId");

            if (orderIdParam == null || orderIdParam.isEmpty()) {
                response.getWriter().print("{\"success\":false,\"message\":\"Thiếu thông tin đơn hàng\"}");
                return;
            }

            int orderId = Integer.parseInt(orderIdParam);

            // Kiểm tra đơn hàng có thuộc về user không
            if (!orderService.isOrderBelongToUser(orderId, userId)) {
                response.getWriter().print("{\"success\":false,\"message\":\"Không có quyền mua lại đơn hàng này\"}");
                return;
            }

            // Lấy thông tin chi tiết đơn hàng
            List<OrderDetail> orderDetails = orderDetailService.getOrderDetailsWithProduct(orderId);

            if (orderDetails == null || orderDetails.isEmpty()) {
                response.getWriter()
                        .print("{\"success\":false,\"message\":\"Không tìm thấy sản phẩm trong đơn hàng\"}");
                return;
            }

            // Thêm sản phẩm vào giỏ hàng thông qua session
            HttpSession session = request.getSession(true);

            // Lấy cart hiện tại hoặc tạo mới
            @SuppressWarnings("unchecked")
            java.util.Map<Integer, Integer> cart = (java.util.Map<Integer, Integer>) session.getAttribute("cart");
            if (cart == null) {
                cart = new java.util.LinkedHashMap<>();
            }

            // Thêm các sản phẩm từ đơn hàng cũ vào cart
            for (OrderDetail detail : orderDetails) {
                int variantId = detail.getVariantId();
                int quantity = detail.getQuantity();
                // Nếu sản phẩm đã có trong cart, cộng thêm số lượng; nếu chưa có thì thêm mới
                cart.put(variantId, cart.getOrDefault(variantId, 0) + quantity);
            }

            // Lưu cart trở lại session
            session.setAttribute("cart", cart);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter()
                    .print("{\"success\":true,\"message\":\"Sản phẩm đã được thêm vào giỏ hàng\",\"redirectUrl\":\""
                            + request.getContextPath() + "/cart" + "\"}");

        } catch (NumberFormatException e) {
            response.getWriter().print("{\"success\":false,\"message\":\"Mã đơn hàng không hợp lệ\"}");
        } catch (Exception e) {
            response.getWriter().print(
                    "{\"success\":false,\"message\":\"Có lỗi xảy ra: " + e.getMessage().replace("\"", "\\\"") + "\"}");
            e.printStackTrace();
        }
    }
}