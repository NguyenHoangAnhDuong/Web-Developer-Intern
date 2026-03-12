package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.DashboardDao;
import vn.edu.hcmuaf.fit.ttltw.dao.DashboardDaoImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardServiceImpl implements DashboardService {

    private final DashboardDao dashboardDao = new DashboardDaoImpl();

    @Override
    public BigDecimal getTodayRevenue() {
        return dashboardDao.getTodayRevenue();
    }

    @Override
    public int getNewOrders() {
        return dashboardDao.getNewOrders();
    }

    @Override
    public int getVisitors() {
        return dashboardDao.getVisitors();
    }

    @Override
    public int getOutOfStock() {
        return dashboardDao.getOutOfStock();
    }

    @Override
    public List<Map<String, Object>> getRevenueByDays(int days) {
        return dashboardDao.getRevenueByDays(days);
    }

    @Override
    public List<Map<String, Object>> getRevenueByCategory() {
        return dashboardDao.getRevenueByCategory();
    }

    @Override
    public List<Map<String, Object>> getTopProducts() {
        return dashboardDao.getTopProducts();
    }

    @Override
    public List<Map<String, Object>> getRecentUsers() {
        return dashboardDao.getRecentUsers();
    }
}