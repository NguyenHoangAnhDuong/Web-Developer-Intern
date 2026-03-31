<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    // Lấy dữ liệu đã format từ servlet
    List<Map<String, Object>> ordersData = (List<Map<String, Object>>) request.getAttribute("ordersData");
    String currentStatus = (String) request.getAttribute("currentStatus");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đơn Mua - Cửa hàng điện thoại</title>

    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/accountSidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/order.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/notification.css">
</head>

<body data-context-path="${pageContext.request.contextPath}">
<jsp:include page="/views/includes/header.jsp"/>

<div id="pageWrapper">
    <!-- SIDEBAR -->
    <jsp:include page="/views/includes/sidebarUser.jsp"/>

    <!-- ================== MAIN CONTENT ================== -->
    <div class="orders-section">
        <h2>Đơn Mua Của Tôi</h2>

        <!-- Thanh trạng thái -->
        <div class="status-tabs">
            <button class="tab <%= (currentStatus == null || "all".equals(currentStatus)) ? "active" : "" %>"
                    data-status="all"
                    onclick="location.href='<%= request.getContextPath() %>/user/order?status=all'">Tất cả</button>
            <button class="tab <%= "1".equals(currentStatus) ? "active" : "" %>"
                    data-status="prepare"
                    onclick="location.href='<%= request.getContextPath() %>/user/order?status=1'">Đang lên đơn</button>
            <button class="tab <%= "2".equals(currentStatus) ? "active" : "" %>"
                    data-status="shipping"
                    onclick="location.href='<%= request.getContextPath() %>/user/order?status=2'">Đang giao</button>
            <button class="tab <%= "3".equals(currentStatus) ? "active" : "" %>"
                    data-status="delivered"
                    onclick="location.href='<%= request.getContextPath() %>/user/order?status=3'">Đã giao</button>
            <button class="tab <%= "4".equals(currentStatus) ? "active" : "" %>"
                    data-status="cancelled"
                    onclick="location.href='<%= request.getContextPath() %>/user/order?status=4'">Đã hủy</button>
        </div>

        <!-- Danh sách đơn -->
        <div class="orders-list">
            <%
                if (ordersData == null || ordersData.isEmpty()) {
            %>
            <div style="text-align: center; padding: 50px; color: #666;">
                <i class="fa-solid fa-box-open" style="font-size: 60px; color: #ddd;"></i>
                <p style="margin-top: 20px; font-size: 16px;">Chưa có đơn hàng nào</p>
            </div>
            <%
            } else {
                for (Map<String, Object> orderMap : ordersData) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> items = (List<Map<String, Object>>) orderMap.get("items");
                    String statusClass = (String) orderMap.get("statusClass");
                    String statusName = (String) orderMap.get("statusName");
                    String statusIcon = (String) orderMap.get("statusIcon");
                    int orderId = (Integer) orderMap.get("id");
                    int status = (Integer) orderMap.get("status");
                    String formattedTotal = (String) orderMap.get("formattedTotal");
            %>

            <!-- Đơn hàng -->
            <div class="order-card" data-status="<%= statusClass %>" data-order-id="<%= orderId %>">
                <%
                    if (items.size() > 1) {
                %>
                <div class="order-items">
                    <% for (Map<String, Object> item : items) { %>
                    <a href="<%= request.getContextPath() %>/user/order-detail?orderId=<%= orderId %>">
                        <div class="order-info">
                            <img src="${pageContext.request.contextPath}/assert/img/product/<%= item.get("imagePath") %>"
                                 alt="<%= item.get("productName") %>">
                            <div class="order-detail">
                                <h3><%= item.get("productName") %> <%= item.get("variantName") %></h3>
                                <p>Số lượng: <%= item.get("quantity") %></p>
                                <p>Giá: <span class="price"><%= item.get("formattedPrice") %>₫</span></p>
                            </div>
                        </div>
                    </a>
                    <% } %>
                </div>
                <%
                } else if (!items.isEmpty()) {
                    Map<String, Object> item = items.get(0);
                %>
                <a href="<%= request.getContextPath() %>/user/order-detail?orderId=<%= orderId %>">
                    <div class="order-info">
                        <img src="${pageContext.request.contextPath}/assert/img/product/<%= item.get("imagePath") %>"
                             alt="<%= item.get("productName") %>">
                        <div class="order-detail">
                            <h3><%= item.get("productName") %> <%= item.get("variantName") %></h3>
                            <p>Số lượng: <%= item.get("quantity") %></p>
                            <p>Giá: <span class="price"><%= item.get("formattedPrice") %>₫</span></p>
                        </div>
                    </div>
                </a>
                <% } %>

                <div class="order-footer">
                    <p class="total">Tổng cộng:
                        <span class="total-price"><%= formattedTotal %>₫</span>
                    </p>
                    <div class="order-status">
            <span class="status <%= statusClass %>">
              <i class="<%= statusIcon %>"></i> <%= statusName %>
            </span>
                        <div class="actions">
                            <% if (status == 1) { %>
                            <button class="btn repurchase" onclick="cancelOrder(<%= orderId %>)">Hủy</button>
                            <% } else if (status == 3 || status == 4) { %>
                            <button class="btn repurchase" onclick="repurchaseOrder(<%= orderId %>)">Mua lại</button>
                            <% } %>
                        </div>
                    </div>
                </div>
            </div>

            <%
                    } // end for
                } // end else
            %>
        </div>
    </div>
</div>

<!-- Toast Container -->
<div id="toast-container" style="position: fixed; top: 20px; right: 20px; z-index: 9999;"></div>
<jsp:include page="/views/includes/toast.jsp"/>
<script src="${pageContext.request.contextPath}/asset/js/notification.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/order.js"></script>

<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/info-user.js"></script>

</body>
</html>