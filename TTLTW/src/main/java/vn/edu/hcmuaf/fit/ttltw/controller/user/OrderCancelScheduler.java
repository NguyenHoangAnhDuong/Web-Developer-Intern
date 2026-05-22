package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import vn.edu.hcmuaf.fit.ttltw.dao.OrderDao;
import vn.edu.hcmuaf.fit.ttltw.model.Order;
import vn.edu.hcmuaf.fit.ttltw.service.SuperAIService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class OrderCancelScheduler implements ServletContextListener {
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                OrderDao orderDao = new OrderDao();
                SuperAIService superAIService = new SuperAIService();
                // hủy các đơn status=0  chờ xác nhận  quá 3 phút chưa thanh toán
                List<Order> expiredOrders = orderDao.getExpiredOrdersForAutoCancel(1);
                for (Order order : expiredOrders) {
                    String trackingCode = order.getTrackingNumber();
                    if (trackingCode == null || trackingCode.isBlank()) {
                        continue;
                    }

                    boolean cancelledOnShip = superAIService.cancelOrder(trackingCode);
                    if (cancelledOnShip) {
                        orderDao.updateOrderStatus(order.getId(), 4);
                    } else {
                    }
                }
            
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) scheduler.shutdownNow();
    }
}
