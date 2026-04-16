package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.Feedback;
import vn.edu.hcmuaf.fit.ttltw.model.Product;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.FeedBackService;
import vn.edu.hcmuaf.fit.ttltw.service.OrderDetailService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductServiceImpl;
import java.io.IOException;

@WebServlet(name = "ReviewServlet", urlPatterns = { "/review" })
public class FeedbackServlet extends HttpServlet {
    private FeedBackService feedbackService;
    private ProductService productService;
    private OrderDetailService orderDetailService;
    @Override
    public void init() {
        feedbackService = new FeedBackService();
        productService = new ProductServiceImpl();
        orderDetailService = new OrderDetailService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String productIdParam = request.getParameter("productId");
        if (productIdParam == null || productIdParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        int productId;
        try {
            productId = Integer.parseInt(productIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        Product product = productService.findProductDetailById(productId);
        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        boolean canReview = orderDetailService.hasUserPurchasedProduct(user.getId(), productId);
        request.setAttribute("product", product);
        request.setAttribute("canReview", canReview);
        request.getRequestDispatcher("/views/user/review.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String productIdParam = request.getParameter("productId");
        String ratingParam = request.getParameter("rating");
        String comment = request.getParameter("comment");
        int productId;
        int rating;
        try {
            productId = Integer.parseInt(productIdParam);
            rating = Integer.parseInt(ratingParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productIdParam);
            return;
        }
        if (rating < 1 || rating > 5) {
            session.setAttribute("toastMessage", "Vui lòng chọn số sao từ 1 đến 5.");
            session.setAttribute("toastType", "error");
            response.sendRedirect(request.getContextPath() + "/review?productId=" + productId);
            return;
        }
        if (!orderDetailService.hasUserPurchasedProduct(user.getId(), productId)) {
            session.setAttribute("toastMessage", "Chỉ có thể đánh giá sản phẩm bạn đã mua.");
            session.setAttribute("toastType", "error");
            response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
            return;
        }
        Feedback feedback = new Feedback();
        feedback.setProductId(productId);
        feedback.setUserId(user.getId());
        feedback.setRating(rating);
        feedback.setComment(comment != null ? comment.trim() : "");
        feedback.setStatus(1);
        boolean success = feedbackService.insertFeedback(feedback);
        if (success) {
            session.setAttribute("toastMessage", "Đã gửi đánh giá. Cảm ơn bạn!");
            session.setAttribute("toastType", "success");
        } else {
            session.setAttribute("toastMessage", "Gửi đánh giá thất bại. Vui lòng thử lại.");
            session.setAttribute("toastType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
    }
}
