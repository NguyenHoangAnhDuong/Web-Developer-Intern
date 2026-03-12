package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.ProductService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@WebServlet("/listproduct_accessory")
public class AccessoryListServlet extends HttpServlet {
    private final ProductService productService = new ProductServiceImpl();
    private static final int DEFAULT_PAGE_SIZE = 12; // Số sản phẩm mỗi trang
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Hiển thị linh kiện (Category ID != 1)
        // Lấy các tham số lọc
        Double priceMin = getDoubleParameter(request, "priceMin");
        Double priceMax = getDoubleParameter(request, "priceMax");
        List<String> categoryNames = getListParameter(request, "categoryName");
        String brandName = request.getParameter("brandName");
        String condition = request.getParameter("condition");
        List<String> models = getListParameter(request, "model");
        String sortBy = request.getParameter("sort");
        String search = request.getParameter("search");

        // Lấy tham số phân trang
        int page = getIntegerParameter(request, "page") != null ? getIntegerParameter(request, "page") : 1;
        int pageSize = DEFAULT_PAGE_SIZE;
        if (page < 1)
            page = 1;

        // Lấy danh sách linh kiện với bộ lọc (tất cả category_id != 1)
        List<Map<String, Object>> allAccessories;
        if (hasFilters(priceMin, priceMax, categoryNames, brandName, condition, models, sortBy)
                || (search != null && !search.trim().isEmpty())) {
            allAccessories = productService.getAccessoriesWithFilters(
                    priceMin, priceMax, brandName, null, condition, sortBy);

            // Lọc theo tên category
            if (categoryNames != null && !categoryNames.isEmpty()) {
                allAccessories = allAccessories.stream()
                        .filter(product -> {
                            String productCategoryName = (String) product.get("categoryName");
                            if (productCategoryName == null)
                                return false;
                            return categoryNames.contains(productCategoryName);
                        })
                        .collect(toList());
            }
            // Lọc theo model sau khi lấy dữ liệu (nếu có)
            if (models != null && !models.isEmpty()) {
                allAccessories = allAccessories.stream()
                        .filter(product -> {
                            String productName = (String) product.get("name");
                            if (productName == null)
                                return false;
                            return models.stream()
                                    .anyMatch(model -> productName.toLowerCase().contains(model.toLowerCase()));
                        })
                        .collect(toList());
            }
            // Lọc theo search nếu có
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                allAccessories = allAccessories.stream()
                        .filter(product -> {
                            String productName = (String) product.get("name");
                            if (productName == null)
                                return false;
                            return productName.toLowerCase().contains(searchLower);
                        })
                        .collect(toList());
            }
        } else {
            allAccessories = productService.getAccessories();
        }

        // Tính toán phân trang đơn giản
        int totalItems = allAccessories.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages < 1)
            totalPages = 1;
        if (page > totalPages)
            page = totalPages;

        // Lấy danh sách cho trang hiện tại
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        List<Map<String, Object>> accessories = new ArrayList<>();
        if (startIndex < totalItems) {
            accessories = allAccessories.subList(startIndex, endIndex);
        }

        // Lấy danh sách categories (linh kiện) từ database
        List<Map<String, Object>> accessoryCategories = productService.getAccessoryCategories();

        // Truyền dữ liệu vào JSP
        request.setAttribute("accessories", accessories);
        request.setAttribute("accessoryCategories", accessoryCategories);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("search", search);
        request.getRequestDispatcher("/views/user/listproduct_accessory.jsp").forward(request, response);
    }

    // Lấy tham số kiểu Double từ request
    private Double getDoubleParameter(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // Lấy tham số kiểu Integer từ request
    private Integer getIntegerParameter(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // Lấy danh sách tham số từ request
    private List<String> getListParameter(HttpServletRequest request, String paramName) {
        String[] values = request.getParameterValues(paramName);
        if (values != null && values.length > 0) {
            return new ArrayList<>(Arrays.asList(values));
        }
        return null;
    }

    // Kiểm tra xem có bộ lọc nào được sử dụng không
    private boolean hasFilters(Double priceMin, Double priceMax, List<String> categoryNames,
            String brandName, String condition, List<String> models, String sortBy) {
        return priceMin != null || priceMax != null || (categoryNames != null && !categoryNames.isEmpty()) ||
                (brandName != null && !brandName.trim().isEmpty()) || (condition != null && !condition.trim().isEmpty())
                ||
                (models != null && !models.isEmpty()) ||
                (sortBy != null && !sortBy.trim().isEmpty());
    }
}