package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.model.Feedback;
import vn.edu.hcmuaf.fit.ttltw.model.Product;
import vn.edu.hcmuaf.fit.ttltw.service.FeedBackService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductServiceImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/feedback")
public class FeedbackDetailServlet extends HttpServlet {

    private ProductService productService;
    private FeedBackService feedBackService;
    @Override
    public void init() {
        productService = new ProductServiceImpl();
        feedBackService = new FeedBackService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("productId");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        int productId;
        try {
            productId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        Product product = productService.findProductDetailById(productId);
        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        Integer filterStar = null;
        String starParam = request.getParameter("star");
        if (starParam != null && !starParam.isEmpty()) {
            try {
                int s = Integer.parseInt(starParam);
                if (s >= 1 && s <= 5) filterStar = s;
            } catch (NumberFormatException ignored) {}
        }
        // Gọi FeedBackService
        List<Feedback> displayFeedbacks      = feedBackService.getFeedbacks(productId, filterStar);
        int totalFeedbacks                   = feedBackService.countByProductId(productId);
        double averageRating                 = feedBackService.getAverageRating(productId);
        Map<String, Integer> starCounts     = feedBackService.getStarCounts(productId);

        request.setAttribute("product",        product);
        request.setAttribute("feedbacks",      displayFeedbacks);
        request.setAttribute("totalFeedbacks", totalFeedbacks);
        request.setAttribute("averageRating",  averageRating);
        request.setAttribute("starCounts",     starCounts);
        request.setAttribute("filterStar",     filterStar);

        request.getRequestDispatcher("/views/user/reviewDetail.jsp")
                .forward(request, response);
    }
}