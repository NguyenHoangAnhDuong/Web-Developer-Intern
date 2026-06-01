package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import vn.edu.hcmuaf.fit.ttltw.model.*;
import vn.edu.hcmuaf.fit.ttltw.service.ProductService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductServiceImpl;
import vn.edu.hcmuaf.fit.ttltw.utils.CloudinaryUtil;
import vn.edu.hcmuaf.fit.ttltw.utils.PermissionUtil;
import vn.edu.hcmuaf.fit.ttltw.validation.ProductPriceValidator;

import java.io.IOException;
import java.util.Map;

@WebServlet("/admin/products/edit")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class ProductEditServlet  extends HttpServlet {
    private ProductService productService = new ProductServiceImpl();

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!PermissionUtil.has(req, "product.update")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền sửa sản phẩm");
            return;
        }
        int vcId;
        try {
            vcId = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ");
            return;
        }
        Map<String, Object> productMap = productService.getProductForEditByVariantColorId(vcId);

        // Gán vào biến 'product' cho form Phone
        req.setAttribute("product", productMap);

        // Kiểm tra category_id (1 là điện thoại)
        Object categoryId = productMap.get("category_id");
        boolean isPhone = categoryId != null && String.valueOf(categoryId).equals("1");
        req.setAttribute("isPhone", isPhone);

        // Nếu là linh kiện, gán thêm biến 'accessory' để JSP nhận diện đúng
        if (!isPhone) {
            req.setAttribute("accessory", productMap);
        }

        req.getRequestDispatcher("/views/admin/editProductAdmin.jsp").forward(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!PermissionUtil.has(req, "product.update")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền sửa sản phẩm");
            return;
        }
        try {
            int productId = Integer.parseInt(req.getParameter("productId"));
            int categoryId = Integer.parseInt(req.getParameter("categoryId"));
            Part imagePart = req.getPart("image");

            Product p = new Product();
            p.setId(productId);
            p.setName(req.getParameter("productName"));
            p.setDescription(req.getParameter("description"));
            if (imagePart != null && imagePart.getSize() > 0) {
                p.setMainImage(CloudinaryUtil.uploadImage(imagePart, "products"));
            } else {
                p.setMainImage(req.getParameter("currentImage"));
            }
            p.setCategory(new Category(categoryId));

            if (categoryId == 1) {
                ProductPriceValidator.ValidationResult basePriceResult =
                        ProductPriceValidator.validatePositivePrice(req.getParameter("basePrice"), "Giá phiên bản (Cơ bản)");
                if (!basePriceResult.ok) {
                    throw new IllegalArgumentException(basePriceResult.message);
                }
                ProductPriceValidator.ValidationResult colorPriceResult =
                        ProductPriceValidator.validatePositivePrice(req.getParameter("colorPrice"), "Giá theo màu");
                if (!colorPriceResult.ok) {
                    throw new IllegalArgumentException(colorPriceResult.message);
                }
            } else {
                ProductPriceValidator.ValidationResult accessoryPriceResult =
                        ProductPriceValidator.validatePositivePriceArray(req.getParameterValues("colorPrices[]"), "Giá bán");
                if (!accessoryPriceResult.ok) {
                    throw new IllegalArgumentException(accessoryPriceResult.message);
                }
            }
            
            String discountStr = req.getParameter("discountPercentage");
            int discount = 0;
            if (discountStr != null && !discountStr.isEmpty()) {
                discount = Integer.parseInt(discountStr);
            }
            p.setDiscountPercentage(discount);
            String[] techNames = req.getParameterValues("techNames[]");
            String[] techValues = req.getParameterValues("techValues[]");
            String[] techPriorities = req.getParameterValues("techPriorities[]");
            if (categoryId == 1) {
                String vName      = req.getParameter("variantName");
                String basePrice  = req.getParameter("basePrice");
                String variantId  = req.getParameter("variantId");
                String colorId    = req.getParameter("colorId");
                String quantity   = req.getParameter("quantity");
                String sku        = req.getParameter("sku");
                String colorPrice = req.getParameter("colorPrice");


                productService.updateProduct(
                        p,
                        techNames, techValues, techPriorities,
                        new String[]{vName},
                        new String[]{basePrice},
                        new String[]{variantId},
                        new String[]{colorId},
                        new String[]{sku},
                        new String[]{quantity},
                        new String[]{colorPrice}
                );
            }
            else {
                String[] vNames = req.getParameterValues("variantNames[]");
                String[] vIds = req.getParameterValues("variantIds[]");
                String[] cIds = req.getParameterValues("colorIds[]");
                String[] qtys = req.getParameterValues("variantQuantities[]");
                String[] cPrices = req.getParameterValues("colorPrices[]");

                productService.updateProduct(
                        p,
                        techNames, techValues, techPriorities,
                        vNames,
                        cPrices,     // base price = color price
                        vIds,
                        cIds,
                        null,
                        qtys,
                        cPrices
                );
            }

            HttpSession session = req.getSession();
            session.setAttribute("toastMessage", "Đã chỉnh sửa");
            session.setAttribute("toastType", "success");

            resp.sendRedirect(req.getContextPath() + "/admin/products?status=success");

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/products?status=error");
        }
    }

}