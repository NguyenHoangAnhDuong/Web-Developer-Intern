package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.config.VnPayConfig;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/placeOrder")
public class CheckoutServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartServiceImpl();
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            // Lấy dữ liệu từ form với validation
            String addressIdStr = request.getParameter("addressId");
            String paymentMethod = request.getParameter("payment");
            String voucherCode = request.getParameter("appliedVoucher");
            String buyerNote = request.getParameter("buyerNote");

            // nếu thanh toán bằng COD thì gửi thông tin cho superAI
            String fullName = request.getParameter("fullName");
            String phone = request.getParameter("phone");
            String fullAddress = request.getParameter("fullAddress");
            String finalTotalStr = request.getParameter("finalTotal");
            String shippingFeeStr = request.getParameter("shippingFee");


            // Kiểm tra addressId
            if (addressIdStr == null || addressIdStr.trim().isEmpty() || paymentMethod == null || finalTotalStr == null) {
                session.setAttribute("toastMessage", "Thiếu thông tin thanh toán");
                session.setAttribute("toastType", "error");
                response.sendRedirect("cart?action=checkout&error=missing_info");
                return;
            }
            double finalTotal = Double.parseDouble(finalTotalStr);
            double shippingFee = 0.0;
            if (shippingFeeStr != null && !shippingFeeStr.trim().isEmpty()) {
                shippingFee = Double.parseDouble(shippingFeeStr);
            }
            int addressId = Integer.parseInt(addressIdStr);
            // Xử lý voucher  có thể không áp dụng
            if (voucherCode != null && voucherCode.trim().isEmpty()) {
                voucherCode = null;
            }
            // Lấy giỏ hàng hiện tại của người dùng
            List<Map<String, Object>> cart = cartService.getCartForDisplay(user.getId());
            if (cart == null || cart.isEmpty()) {
                session.setAttribute("toastMessage", "Giỏ hàng của bạn đang trống");
                session.setAttribute("toastType", "error");
                response.sendRedirect("cart?action=view");
                return;
            }

            // Lấy danh sách selectedItems từ form
            String[] selectedItems = request.getParameterValues("selectedItems");
            Map<Integer, Integer> checkoutCart = new LinkedHashMap<>();

            if (selectedItems != null ) {
                // Lọc chỉ sản phẩm được chọn
                for (String selectedId : selectedItems) {
                    try {
                        int id = Integer.parseInt(selectedId.trim());
                        // Tìm sản phẩm trong cart dựa trên vc_id và lấy quantity
                        for (Map<String, Object> item : cart) {
                            int vcId = Integer.parseInt(item.get("vc_id").toString());

                            if (vcId == id) {
                                int qty = Integer.parseInt(item.get("quantity").toString());
                                checkoutCart.put(vcId, qty);
                                break;
                            }
                    }} catch (NumberFormatException e) {
                        System.out.println("Lỗi định dạng selectedItems: " + selectedId);
                    }
                }
            }
            // Nếu không có selectedItems hoặc lọc không ra được gì
            if (checkoutCart.isEmpty()) {
                throw new Exception("Không có sản phẩm được chọn");
             }
                // xác định trạng thái thanh toán
                int paymentStatus = "bank".equalsIgnoreCase(paymentMethod) ? 2 : 1;

                // gọi xử lý đặt hàng
                int orderId = orderService.processOrder(user.getId(), addressId, paymentMethod, voucherCode,
                        checkoutCart, paymentStatus, buyerNote,shippingFee);

                if (orderId > 0) {
                    // Xóa giỏ hàng thành công
                    for (Integer vcId : checkoutCart.keySet()) cartService.removeCartItem(user.getId(), vcId);
                    // Chuyển hướng theo phương thức thanh toán
                    if ("bank".equalsIgnoreCase(paymentMethod)) {
                        // vnpay
                        String vnpUrl = VnPayConfig.createPaymentUrl(orderId, finalTotal, request);
                        response.sendRedirect(vnpUrl);
                        return;
                    } else {
                            orderService.handleShippingAsync(orderId, fullName, phone, fullAddress, finalTotal);
                        }
                        session.setAttribute("toastMessage", "Đặt hàng thành công!");
                        session.setAttribute("toastType", "success");
                        response.sendRedirect(request.getContextPath() + "/user/order-detail?orderId=" + orderId);

                    }
        }  catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("toastMessage", e.getMessage() != null ? e.getMessage() : "Có lỗi hệ thống xảy ra");
            session.setAttribute("toastType", "error");
            response.sendRedirect("cart?action=checkout&error=system_error");
        }}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("cart?action=view");
    }
}