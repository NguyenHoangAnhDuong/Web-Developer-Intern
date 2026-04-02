package vn.edu.hcmuaf.fit.ttltw.controller.user;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import vn.edu.hcmuaf.fit.ttltw.dao.ShippingDao;

@WebServlet("/api/shipping/webhook")
public class ShippingWebhookServlet extends HttpServlet {
    private final ShippingDao shippingDao = new ShippingDao();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // đọc nội dung từ json
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        try {
            JsonObject json = JsonParser.parseString(buffer.toString()).getAsJsonObject();

            // lấy mã đơn hàng và trạng thái mới
            String trackingCode = json.get("code").getAsString();
            String statusName = json.get("status_name").getAsString();

            // tìm orderId dựa trên trackingCode và cập nhật
            int orderId = shippingDao.getOrderIdByTracking(trackingCode);

            if (orderId > 0) {
                shippingDao.updateOnlyShippingStatus(orderId, statusName);
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}