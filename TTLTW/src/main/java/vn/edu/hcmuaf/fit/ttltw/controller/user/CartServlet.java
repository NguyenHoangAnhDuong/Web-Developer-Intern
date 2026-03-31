package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.Address;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.model.Voucher;
import vn.edu.hcmuaf.fit.ttltw.service.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private final CartService cartService = new CartServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "view";

        switch (action) {
            case "view":
                showCart(request, response, user.getId());
                break;
            case "add":
                addToCart(request, response, user.getId());
                break;
            case "update":
                updateQuantity(request, response, user.getId());
                break;
            case "remove":
                removeItem(request, response, user.getId());
                break;
            case "clear":
                clearCart(request, response, user.getId());
                break;
            case "changeVariant":
                changeVariant(request, response, user.getId());
                break;
            default:
                response.sendRedirect("home");
                break;
        }
    }
    // Hiển thị trang giỏ hàng
    private void showCart(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        List<Map<String, Object>> items = cartService.getCartForDisplay(userId);
        double total = cartService.calculateCartTotal(userId);

        request.setAttribute("cartItems", items);
        request.setAttribute("totalCartPrice", total);
        request.getRequestDispatcher("/views/user/cart.jsp").forward(request, response);
    }

    // Xử lý thêm vào giỏ
    private void addToCart(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        int vId = Integer.parseInt(request.getParameter("vcId"));
        int quantity = 1; // Mặc định mỗi lần click là 1

        boolean success = cartService.addToCart(userId, vId, quantity);

        if (success) {
            response.getWriter().print("SUCCESS");
        } else {
            response.setStatus(400);
            response.getWriter().print("STOCK_EXCEEDED");
        }
    }
// Cập nhật số lượng
    private void updateQuantity(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        int vId = Integer.parseInt(request.getParameter("vcId"));
        int delta = Integer.parseInt(request.getParameter("delta"));
        boolean success = cartService.updateCartItem(userId, vId, delta);

        if (success) {
            double newTotal = cartService.calculateCartTotal(userId);
            String json = "{\"status\": \"success\", \"newTotal\": " + newTotal + "}";
            response.getWriter().print(json);
        } else {
            response.setStatus(400);
            response.getWriter().print("{\"status\": \"error\", \"message\": \"Hết hàng!\"}");
        }
    }

    // Xóa một món hàng
    private void removeItem(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        int vId = Integer.parseInt(request.getParameter("vcId"));
        cartService.removeCartItem(userId, vId);
        response.sendRedirect("cart?action=view");
    }
// Xóa tất cả món hàng
    private void clearCart(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        cartService.clearCart(userId);
        response.sendRedirect("cart?action=view");
    }
    // Thay đổi biến thể màu/variant
    private void changeVariant(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {

        int oldId = Integer.parseInt(request.getParameter("oldVcId"));
        int newId = Integer.parseInt(request.getParameter("newVcId"));
        boolean success = cartService.changeVariant(userId, oldId, newId);
        response.setContentType("application/json");
        if (success) {
            response.getWriter().print("{\"status\":\"success\"}");
        } else {
            response.setStatus(400);
            response.getWriter().print("{\"status\":\"error\",\"message\":\"Hết hàng\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}