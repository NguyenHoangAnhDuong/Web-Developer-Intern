package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.hcmuaf.fit.ttltw.model.*;
import vn.edu.hcmuaf.fit.ttltw.service.*;
import vn.edu.hcmuaf.fit.ttltw.utils.CloudinaryUtil;
import vn.edu.hcmuaf.fit.ttltw.utils.PermissionUtil;
import vn.edu.hcmuaf.fit.ttltw.validation.ProductPriceValidator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/admin/product/add")
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 50 * 1024 * 1024
)
public class ProductAddEServlet extends HttpServlet {
    private ProductService productService ;
    @Override
    public void init()   {
     productService = new ProductServiceImpl();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionUtil.has(request, "product.create")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền thêm sản phẩm");
            return;
        }
        request.getRequestDispatcher("/views/admin/addProductAdmin.jsp"
        ).forward(request, response);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!PermissionUtil.has(request, "product.create")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền thêm sản phẩm");
            return;
        }
        request.setCharacterEncoding("UTF-8");

        try {
            String name = request.getParameter("productName");
            String description = request.getParameter("description");
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String discountStr = request.getParameter("discountPercentage");
            int discount = 0;
            if (discountStr != null && !discountStr.isBlank()) {
                discount = Integer.parseInt(discountStr);
            }
            String brandParam = request.getParameter("brandId");
            Brand brand;
            if ("custom".equals(brandParam)) {
                brand = new Brand();
                brand.setName(request.getParameter("customBrand"));
            } else {
                brand = new Brand(Integer.parseInt(brandParam));
            }
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setCategory(new Category(categoryId));
            product.setBrand(brand);
            product.setStatus(1);
            product.setCreatedAt(LocalDateTime.now());
            product.setDiscountPercentage(discount);
            Part mainImagePart = request.getPart("productImage");
            if (mainImagePart != null && mainImagePart.getSize() > 0) {
                product.setMainImage(CloudinaryUtil.uploadImage(mainImagePart, "products"));
            }
            String[] techNames = request.getParameterValues("techName[]");
            String[] techValues = request.getParameterValues("techValue[]");
            String[] techPriorities = request.getParameterValues("techPriority[]");

            String[] variantNames = request.getParameterValues("variantName[]");
            String[] basePrices = request.getParameterValues("basePrice[]");
            String[] quantities = request.getParameterValues("quantity[]");
            String[] variantQuantities = request.getParameterValues("variantQuantity[]");
            String[] skus = request.getParameterValues("sku[]");
            String[] colorVariantIndexes = request.getParameterValues("colorVariantIndex[]");
            String[] colorIds = request.getParameterValues("colorId[]");
            String[] customColors = request.getParameterValues("customColor[]");
            String[] colorPrices = request.getParameterValues("colorPrice[]");

            ProductPriceValidator.ValidationResult basePriceResult = ProductPriceValidator.validatePositivePriceArray(basePrices, "Giá gốc");
            if (!basePriceResult.ok) {
                throw new IllegalArgumentException(basePriceResult.message);
            }
            if (categoryId == 1) {
                ProductPriceValidator.ValidationResult colorPriceResult = ProductPriceValidator.validatePositivePriceArray(colorPrices, "Giá màu");
                if (!colorPriceResult.ok) {
                    throw new IllegalArgumentException(colorPriceResult.message);
                }
            }

            Map<String, List<Image>> colorImagesMap = new HashMap<>();
            for (Part part : request.getParts()) {
                if (part.getName().startsWith("colorImages_") && part.getSize() > 0) {

                    String[] partsName = part.getName().split("_");

                    int variantIndex = Integer.parseInt(partsName[1]);
                    int colorIndex   = Integer.parseInt(partsName[2]);

                        String fileName = CloudinaryUtil.uploadImage(part, "products/colors");
                    String key = variantIndex + "_" + colorIndex;
                    colorImagesMap
                            .computeIfAbsent(key, k -> new ArrayList<>())
                            .add(new Image(fileName));
                }
            }
            System.out.println("colorVariantIndexes = " + Arrays.toString(colorVariantIndexes));
            System.out.println("colorImagesMap keys = " + colorImagesMap.keySet());

            productService.addProduct(product, techNames, techValues, techPriorities,
                    variantNames, basePrices, quantities, variantQuantities,
                    skus, colorVariantIndexes, colorIds, customColors, colorPrices , colorImagesMap);


            HttpSession session = request.getSession();
            session.setAttribute("toastMessage", "Thêm sản phẩm thành công!");
            session.setAttribute("toastType", "success");
            response.sendRedirect(request.getContextPath() + "/admin/products?status=success");
        } catch (Exception e) {
            e.printStackTrace();
            HttpSession session = request.getSession();
            session.setAttribute("toastMessage", e.getMessage());
            session.setAttribute("toastType", "error");

            response.sendRedirect(
                    request.getContextPath() + "/admin/product/add"
            );
        }
    }

}
