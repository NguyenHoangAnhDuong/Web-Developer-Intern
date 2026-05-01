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
        // Lấy tham số thời gian từ request
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
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

        List<Map<String, Object>> revenueByDays;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            revenueByDays = dashboardService.getRevenueByDateRange(startDate, endDate);
        } else {
            revenueByDays = dashboardService.getRevenueByDays(days);
        }
        // Xác định khoảng thời gian cho các biểu đồ khác
        String filterStartDate = startDate;
        String filterEndDate = endDate;
        if (filterStartDate == null || filterStartDate.isEmpty()) {
            // Nếu không có khoảng thời gian tùy chỉnh, dùng 30 ngày mặc định
            java.time.LocalDate today = java.time.LocalDate.now();
            filterStartDate = today.minusDays(30).toString();
            filterEndDate = today.toString();
        }
        List<Map<String, Object>> revenueByCategory = dashboardService.getRevenueByCategory(filterStartDate, filterEndDate);
        List<Map<String, Object>> topProducts = dashboardService.getTopProducts(filterStartDate, filterEndDate);
        List<Map<String, Object>> recentUsers = dashboardService.getRecentUsers();

        // Set attributes cho JSP
        req.setAttribute("todayRevenue", todayRevenue);
        req.setAttribute("newOrders", newOrders);
        req.setAttribute("visitors", visitors);
        req.setAttribute("outOfStock", outOfStock);
        req.setAttribute("days", days);
        req.setAttribute("startDate", startDate);
        req.setAttribute("endDate", endDate);
        req.setAttribute("revenueByDays", revenueByDays);
        req.setAttribute("revenueByCategory", revenueByCategory);
        req.setAttribute("topProducts", topProducts);
        req.setAttribute("recentUsers", recentUsers);

        // Forward đến dashboard.jsp
        req.getRequestDispatcher("/views/admin/dashboard.jsp").forward(req, resp);
    }
}