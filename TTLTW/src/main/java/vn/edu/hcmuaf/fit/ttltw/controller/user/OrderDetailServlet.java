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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "OrderDetailServlet", urlPatterns = { "/user/order-detail" })
public class OrderDetailServlet extends HttpServlet {

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
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        // Lấy orderId từ parameter
        String orderIdParam = request.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/user/order");
            return;
        }
        try {
            int orderId = Integer.parseInt(orderIdParam);

            // Kiểm tra đơn hàng có thuộc về user không
            if (!orderService.isOrderBelongToUser(orderId, user.getId())) {
                response.sendRedirect(request.getContextPath() + "/user/order");
                return;
            }
            // Lấy thông tin đơn hàng
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (!orderOpt.isPresent()) {
                response.sendRedirect(request.getContextPath() + "/user/order");
                return;
            }
            Order order = orderOpt.get();

            // Lấy chi tiết đơn hàng
            List<OrderDetail> orderDetails = orderDetailService.getOrderDetailsWithProduct(orderId);

            // Lấy thông tin địa chỉ giao hàng
            Address address = orderService.getOrderAddress(order.getAddressId());

            // Lấy thông tin phương thức thanh toán
            PaymentTypes paymentType = orderService.getPaymentType(order.getPaymentTypeId());

            // Format dữ liệu cho JSP
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            // Tính subtotal và format tất cả items
            double subtotal = 0;
            List<Map<String, Object>> itemsList = new ArrayList<>();
            if (orderDetails != null && !orderDetails.isEmpty()) {
                for (OrderDetail detail : orderDetails) {
                    subtotal += detail.getTotalMoney();
                    Map<String, String> productInfo = orderDetailService
                            .getProductInfoByVariantId(detail.getVariantId());

                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("id", detail.getId());
                    itemMap.put("quantity", detail.getQuantity());
                    itemMap.put("price", detail.getPrice());
                    itemMap.put("formattedPrice", currencyFormat.format(detail.getPrice()));
                    itemMap.put("productName", productInfo != null ? productInfo.get("productName") : "N/A");
                    itemMap.put("variantName", productInfo != null ? productInfo.get("variantName") : "");
                    itemMap.put("colorName", productInfo != null ? productInfo.get("colorName") : "");
                    itemMap.put("colorCode", productInfo != null ? productInfo.get("colorCode") : "");
                    itemMap.put("productId", productInfo != null ? Integer.parseInt(productInfo.getOrDefault("productId", "0")) : 0);
                    itemMap.put("variantId", detail.getVariantId());
                    itemMap.put("imagePath",
                            productInfo != null && productInfo.get("imagePath") != null ? productInfo.get("imagePath")
                                    : "asset/img/product/default.jpg");
                    itemsList.add(itemMap);
                }
            }

            // Đóng gói thông tin đơn hàng
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("id", order.getId());
            orderData.put("status", order.getStatus());
            orderData.put("createdAt", order.getCreatedAt());
            orderData.put("formattedDate", dateFormat.format(order.getCreatedAt()));
            orderData.put("feeShipping", order.getFeeShipping());
            orderData.put("formattedFeeShipping", currencyFormat.format(order.getFeeShipping()));
            orderData.put("discountAmount", order.getDiscountAmount());
            orderData.put("formattedDiscountAmount", currencyFormat.format(order.getDiscountAmount()));
            orderData.put("totalAmount", order.getTotalAmount());
            orderData.put("formattedTotalAmount", currencyFormat.format(order.getTotalAmount()));
            orderData.put("subtotal", subtotal);
            orderData.put("formattedSubtotal", currencyFormat.format(subtotal));
            orderData.put("statusName", OrderService.getStatusName(order.getStatus()));

            // Thông tin thanh toán
            String paymentStatusText;
            String paymentStatusClass;
            if (paymentType != null && paymentType.getId() == 2) {
                paymentStatusText = "Đã thanh toán";
                paymentStatusClass = "status-paid";
            } else {
                paymentStatusText = "Chưa thanh toán";
                paymentStatusClass = "status-unpaid";
            }
            orderData.put("paymentStatusText", paymentStatusText);
            orderData.put("paymentStatusClass", paymentStatusClass);
            orderData.put("paymentMethodName", paymentType != null ? paymentType.getName() : "N/A");

            // Thông tin địa chỉ
            Map<String, String> addressData = new HashMap<>();
            if (address != null) {
                addressData.put("name", address.getName());
                addressData.put("address", address.getAddress());
                addressData.put("phoneNumber", address.getPhoneNumber());
            } else {
                addressData.put("name", "N/A");
                addressData.put("address", "N/A");
                addressData.put("phoneNumber", "N/A");
            }

            // Đưa dữ liệu vào request
            request.setAttribute("orderData", orderData);
            request.setAttribute("items", itemsList);
            request.setAttribute("address", addressData);

            // Set sidebar data
            request.setAttribute("activeMenu", "order");
            SidebarUtil.setSidebarData(request);

            // Forward đến JSP
            request.getRequestDispatcher("/views/user/order_detail.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/user/order");
        }
    }
}