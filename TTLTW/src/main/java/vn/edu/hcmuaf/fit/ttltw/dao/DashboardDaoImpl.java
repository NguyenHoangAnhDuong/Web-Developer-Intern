package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardDaoImpl implements DashboardDao {
    private final Jdbi jdbi = DBConnect.getJdbi();

    @Override
    public BigDecimal getTodayRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) AS revenue FROM orders WHERE DATE(created_at) = CURDATE()";
        return jdbi.withHandle(h -> h.createQuery(sql).mapTo(BigDecimal.class).one());
    }

    @Override
    public int getNewOrders() {
        String sql = "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()";
        return jdbi.withHandle(h -> h.createQuery(sql).mapTo(Integer.class).one());
    }

    @Override
    public int getVisitors() {
        // Số user đăng ký hôm nay (tất cả user sử dụng hệ thống)
        String sql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()";
        return jdbi.withHandle(h -> h.createQuery(sql).mapTo(Integer.class).one());
    }

    @Override
    public int getOutOfStock() {
        String sql = "SELECT COUNT(*) FROM variant_colors WHERE quantity <= 0";
        return jdbi.withHandle(h -> h.createQuery(sql).mapTo(Integer.class).one());
    }

    @Override
    public List<Map<String, Object>> getRevenueByDays(int days) {
        String sql = """
                SELECT DATE(created_at) AS date, COALESCE(SUM(total_amount), 0) AS revenue
                FROM orders
                WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                GROUP BY DATE(created_at)
                ORDER BY date
                """;
        return jdbi.withHandle(h -> h.createQuery(sql).bind(0, days).mapToMap().list());
    }

    @Override
    public List<Map<String, Object>> getRevenueByDateRange(String startDate, String endDate) {
        String sql = """
                SELECT DATE(created_at) AS date, COALESCE(SUM(total_amount), 0) AS revenue
                FROM orders
                WHERE DATE(created_at) BETWEEN ? AND ?
                GROUP BY DATE(created_at)
                ORDER BY date
                """;
        return jdbi.withHandle(h -> h.createQuery(sql).bind(0, startDate).bind(1, endDate).mapToMap().list());
    }
    @Override
    public List<Map<String, Object>> getRevenueByCategory(String startDate, String endDate) {
        String sql = """
                SELECT c.name AS category, COALESCE(SUM(od.quantity * od.price), 0) AS revenue
                FROM categories c
                LEFT JOIN products p ON c.id = p.category_id
                LEFT JOIN product_variants pv ON p.id = pv.product_id
                LEFT JOIN variant_colors vc ON pv.id = vc.variant_id
                LEFT JOIN order_details od ON vc.id = od.variant_id
                LEFT JOIN orders o ON od.order_id = o.id
                WHERE DATE(o.created_at) BETWEEN ? AND ?
                GROUP BY c.id, c.name
                ORDER BY revenue DESC
                """;
        return jdbi.withHandle(h -> h.createQuery(sql).bind(0, startDate).bind(1, endDate).mapToMap().list());
    }

    @Override
    public List<Map<String, Object>> getTopProducts(String startDate, String endDate) {
        String sql = """
                SELECT p.name AS product, SUM(od.quantity) AS sold
                FROM products p
                JOIN product_variants pv ON p.id = pv.product_id
                JOIN variant_colors vc ON pv.id = vc.variant_id
                JOIN order_details od ON vc.id = od.variant_id
                JOIN orders o ON od.order_id = o.id
                WHERE DATE(o.created_at) BETWEEN ? AND ?
                GROUP BY p.id, p.name
                ORDER BY sold DESC
                LIMIT 10
                """;
        return jdbi.withHandle(h -> h.createQuery(sql).bind(0, startDate).bind(1, endDate).mapToMap().list());
    }

    @Override
    public List<Map<String, Object>> getRecentUsers() {
        String sql = """
                SELECT CONCAT(LEFT(first_name, 1), LEFT(last_name, 1)) AS initials,
                       CONCAT(first_name, ' ', last_name) AS fullName,
                       email,
                       DATEDIFF(CURDATE(), DATE(created_at)) AS daysAgo
                FROM users
                ORDER BY created_at DESC
                LIMIT 5
                """;
        return jdbi.withHandle(h -> h.createQuery(sql).mapToMap().list());
    }
}