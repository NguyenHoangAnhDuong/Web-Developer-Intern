package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.dao.*;
import vn.edu.hcmuaf.fit.ttltw.model.*;
import vn.edu.hcmuaf.fit.ttltw.service.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ProductDetailServlet", value = "/product-detail")
public class ProductDetailServlet extends HttpServlet {

    private ProductDao productDao;
    private VariantDao variantDao;
    private FeedbackDao feedbackDao;
    private ProductService productService;

    @Override
    public void init() {
        productDao = new ProductDaoImpl();
        variantDao = new VariantDao();
        feedbackDao = new FeedbackDao();
        productService = new ProductServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int productId = 3;
        if (null!=request.getParameter("id")) {
            // Lấy productId
            productId = Integer.parseInt(request.getParameter("id"));
        }
        // Product chính
        Product product = productDao.findProductDetailById(productId);

        // Variants
        List<ProductVariant> variants = productDao.getVariantsByProduct(productId);

        // Tech specs
        List<TechSpecs> techSpecs = productDao.getTechSpecsByProduct(productId);

        //  Default VariantColor (variant_color đầu tiên)
        VariantColor defaultVC = productDao.getDefaultVariantColor(productId);

        // Colors + Images - Lấy từ variant đầu tiên
        List<VariantColor> colors = null;
        List<Image> images = null;
        int firstVariantId = -1;

        // Lấy variant đầu tiên để hiển thị colors mặc định
        if (!variants.isEmpty()) {
            firstVariantId = variants.get(0).getId();
            colors = variantDao.getColorsByVariant(firstVariantId);
        }

        // Lấy images từ defaultVC (variant_color đầu tiên)
        if (defaultVC != null) {
            images = variantDao.getImagesByVariantColor(defaultVC.getId());
        }

        // Lấy tất cả variant_colors để JavaScript có thể truy cập giá
        List<Map<String, Object>> variantColors = ((ProductDaoImpl) productDao).getAllVariantColorsForProduct(productId);
        // Feedback
        List<Feedback> feedbacks = feedbackDao.getFeedbacksByProductId(productId);
        int totalFeedbacks = feedbackDao.countByProductId(productId);

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