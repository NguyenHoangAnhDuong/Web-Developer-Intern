package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.service.ProductService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductServiceImpl;
import vn.edu.hcmuaf.fit.ttltw.utils.PermissionUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/products")
public class ProductServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() {
        productService = new ProductServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!PermissionUtil.has(req, "product.view")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền xem danh sách sản phẩm");
            return;
        }

        String keyword = req.getParameter("keyword");

        Integer status = parseInteger(req.getParameter("status"));
        Integer categoryId = parseInteger(req.getParameter("categoryId"));

        int page = parsePage(req.getParameter("page"));
        int limit = 10;
        int offset = (page - 1) * limit;
        List<Map<String, Object>> products =
                productService.getForAdmin(keyword, status, categoryId, page, limit);

        int total = productService.countForAdmin(keyword, status, categoryId);
        int totalPages = (int) Math.ceil((double) total / limit);

        req.setAttribute("products", products);
        req.setAttribute("totalVariants", total);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("currentPage", page);

        // giữ filter khi render lại form
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("categoryId", categoryId);

        req.getRequestDispatcher("/views/admin/product.jsp")
                .forward(req, resp);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parsePage(String value) {
        try {
            int p = Integer.parseInt(value);
            return p > 0 ? p : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("toggle".equals(action)) {

            HttpSession session = req.getSession();

            if (!PermissionUtil.has(req, "product.update")) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"Không có quyền cập nhật sản phẩm\"}");
                return;
            }

            try {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean result = productService.toggleStatus(id);

                if (result) {
                    session.setAttribute("toastMessage", "Cập nhật trạng thái sản phẩm thành công");
                    session.setAttribute("toastType", "success");
                } else {
                    session.setAttribute("toastMessage", "Không thể cập nhật trạng thái sản phẩm");
                    session.setAttribute("toastType", "error");
                }

            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("toastMessage", "Có lỗi xảy ra khi cập nhật trạng thái");
                session.setAttribute("toastType", "error");
            }

            resp.sendRedirect(req.getContextPath() + "/admin/products");
        }
    }

}
