package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.model.*;
import vn.edu.hcmuaf.fit.ttltw.service.FeedBackService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductServiceImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ProductDetailServlet", value = "/product-detail")
public class ProductDetailServlet extends HttpServlet {

    private FeedBackService feedbackService;
    private ProductService productService;

    @Override
    public void init() {
        feedbackService = new FeedBackService();
        productService = new ProductServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int productId = 3;
        if (request.getParameter("id") != null) {
            productId = Integer.parseInt(request.getParameter("id"));
        }

        Product product = productService.findProductDetailById(productId);
        List<ProductVariant> variants = productService.getVariantsByProduct(productId);
        List<TechSpecs> techSpecs = productService.getTechSpecsByProduct(productId);
        VariantColor defaultVC = productService.getDefaultVariantColor(productId);

        List<VariantColor> colors = null;
        List<Image> images = null;
        int firstVariantId = -1;

        if (!variants.isEmpty()) {
            firstVariantId = variants.get(0).getId();
            colors = productService.getColorsByVariant(firstVariantId);
        }

        if (defaultVC != null) {
            images = productService.getImagesByVariantColor(defaultVC.getId());
        }

        List<Map<String, Object>> variantColors = productService.getAllVariantColorsForProduct(productId);
        List<Feedback> feedbacks = feedbackService.getFeedbacksByProductId(productId);
        int totalFeedbacks = feedbackService.countByProductId(productId);

        List<Map<String, Object>> relatedProducts =
                productService.getRelatedProducts(product.getBrand().getId(), productId, 4);

        request.setAttribute("relatedProducts", relatedProducts);
//        request.getRequestDispatcher("/productDetail.jsp").forward(request, response);
        request.setAttribute("product", product);
        request.setAttribute("variants", variants);
        request.setAttribute("colors", colors);
        request.setAttribute("images", images);
        request.setAttribute("techSpecs", techSpecs);
        request.setAttribute("defaultVariantColor", defaultVC);
        request.setAttribute("variantColors", variantColors);
        request.setAttribute("feedbacks", feedbacks);
        request.setAttribute("totalFeedbacks", totalFeedbacks);
        request.getRequestDispatcher("/views/user/product-detail.jsp")
                .forward(request, response);
    }
}