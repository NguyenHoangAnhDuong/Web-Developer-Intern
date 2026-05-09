package vn.edu.hcmuaf.fit.ttltw.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DashboardDao {
    BigDecimal getTodayRevenue();

    int getNewOrders();

    int getVisitors();

    int getOutOfStock();

    List<Map<String, Object>> getRevenueByDays(int days);

    List<Map<String, Object>> getRevenueByDateRange(String startDate, String endDate);

    List<Map<String, Object>> getRevenueByCategory(String startDate, String endDate);

    List<Map<String, Object>> getTopProducts(String startDate, String endDate);

    List<Map<String, Object>> getRecentUsers();
}