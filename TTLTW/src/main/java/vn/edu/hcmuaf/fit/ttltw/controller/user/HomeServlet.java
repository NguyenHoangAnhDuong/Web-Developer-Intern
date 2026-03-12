package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.HomeService;
import vn.edu.hcmuaf.fit.ttltw.service.HomeServiceImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "HomeServlet", urlPatterns = { "/" })
public class HomeServlet extends HttpServlet {
    private final HomeService homeService = new HomeServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // LẤY SESSION HIỆN TẠI
        HttpSession session = request.getSession(false); // false = không tạo session mới

        if (session != null) {
            User user = (User) session.getAttribute("user");

            request.setAttribute("user", user);
        }
        // Load featured products
        List<Map<String, Object>> featuredProducts = homeService.getFeaturedProducts();
        request.setAttribute("featuredProducts", featuredProducts);

        // Load featured accessories
        List<Map<String, Object>> featuredAccessories = homeService.getFeaturedAccessories();
        request.setAttribute("featuredAccessories", featuredAccessories);

        // Load active vouchers
        List<Map<String, Object>> activeVouchers = homeService.getActiveVouchers();
        request.setAttribute("activeVouchers", activeVouchers);

        // Load banner products
        List<Map<String, Object>> bannerProducts = homeService.getBannerProducts();
        request.setAttribute("bannerProducts", bannerProducts);

        // Disable cache
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Forward to home.jsp
        request.getRequestDispatcher("/views/user/home.jsp").forward(request, response);
    }

}
