package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.DashboardService;
import vn.edu.hcmuaf.fit.ttltw.service.DashboardServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    // Service xử lý dữ liệu dashboard
    private DashboardService dashboardService;

    @Override
    public void init() {
        dashboardService = new DashboardServiceImpl();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Lấy số ngày từ request (mặc định 30 ngày)
        int days = 30;
        String daysParam = req.getParameter("days");
        if (daysParam != null) {
            try {
                days = Integer.parseInt(daysParam);
            } catch (NumberFormatException e) {
                days = 30;
            }
        }

        // Lấy dữ liệu từ service
        BigDecimal todayRevenue = dashboardService.getTodayRevenue();
        int newOrders = dashboardService.getNewOrders();
        int visitors = dashboardService.getVisitors();
        int outOfStock = dashboardService.getOutOfStock();
        List<Map<String, Object>> revenueByDays = dashboardService.getRevenueByDays(days);
        List<Map<String, Object>> revenueByCategory = dashboardService.getRevenueByCategory();
        List<Map<String, Object>> topProducts = dashboardService.getTopProducts();
        List<Map<String, Object>> recentUsers = dashboardService.getRecentUsers();

        // Set attributes cho JSP
        req.setAttribute("todayRevenue", todayRevenue);
        req.setAttribute("newOrders", newOrders);
        req.setAttribute("visitors", visitors);
        req.setAttribute("outOfStock", outOfStock);
        req.setAttribute("days", days);
        req.setAttribute("revenueByDays", revenueByDays);
        req.setAttribute("revenueByCategory", revenueByCategory);
        req.setAttribute("topProducts", topProducts);
        req.setAttribute("recentUsers", recentUsers);

        // Forward đến dashboard.jsp
        req.getRequestDispatcher("/views/admin/dashboard.jsp").forward(req, resp);
    }
}