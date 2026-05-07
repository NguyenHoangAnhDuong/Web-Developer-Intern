<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN"/>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="isAjax" value="${param.ajax == 'true'}"/>
<c:set var="isSuper" value="${sessionScope.roleName == 'super_admin'}" />
<c:set var="perms" value="${sessionScope.permissions}" />
<c:set var="canUpdateOrder" value="${isSuper || (perms != null && perms.contains('order.update'))}" />

<c:if test="${!isAjax}">
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản Lý Đơn Hàng</title>

        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/admin-orders.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">
    </head>
    <body>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>

    <div class="app">
    <%@ include file="/views/includes/sideBarAdmin.jsp" %>

    <main class="content">
    <div class="orders-section">

    <h2>Quản Lý Đơn Hàng</h2>

    <div class="search-filter-bar">
        <div class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="searchInput"
                   placeholder="Tìm theo mã đơn hoặc tên khách hàng">
        </div>

        <div class="filter-options">
            <select id="statusFilter" class="filter-select">
                <option value="">Tất cả trạng thái</option>
                <option value="1">Đang lên đơn</option>
                <option value="2">Đang giao</option>
                <option value="3">Đã giao</option>
                <option value="5">Hủy</option>
            </select>
        </div>
    </div>


    <table class="orders-table" id="ordersTable">
    <thead>
    <tr>
        <th>Mã ĐH</th>
        <th>Khách hàng</th>
        <th>Số điện thoại</th>
        <th>Ngày đặt</th>
        <th>Trạng thái</th>
        <th>Thanh toán</th>
        <th>Tổng</th>
    </tr>
    </thead>
    <tbody>
</c:if>

<%--AJAX--%>
<c:choose>
    <c:when test="${not empty orders}">
        <c:forEach var="item" items="${orders}">
            <tr>
                <td>DH${item.order.id}</td>
                <!-- Lấy tên khách từ Map -->
                <td>${item.customerName}</td>
                <td>${item.customerPhone}</td>
                <td>${item.order.createdAt}</td>
                <td>
                    <select class="status-select status-${item.order.status}" data-id="${item.order.id}" ${canUpdateOrder ? '' : 'disabled'}>
                        <option value="1" ${item.order.status == 1 ? 'selected' : ''}>Đang lên đơn</option>
                        <option value="2" ${item.order.status == 2 ? 'selected' : ''}>Đang giao</option>
                        <option value="3" ${item.order.status == 3 ? 'selected' : ''}>Đã giao</option>
                        <option value="4" ${item.order.status == 4 ? 'selected' : ''}>Đã hủy</option>
                    </select>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${item.order.paymentTypeId == 1}">
                            COD
                        </c:when>
                        <c:otherwise>
                            CK
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <fmt:formatNumber value="${item.order.totalAmount}" type="number" pattern="#,###" groupingUsed="true"/>₫
                </td>
            </tr>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <tr>
            <td colspan="7" style="text-align:center;">Không có đơn hàng nào</td>
        </tr>
    </c:otherwise>
</c:choose>

<c:if test="${!isAjax}">
    </tbody>
    </table>

    </div>
    </main>
    </div>
    <div id="toast" class="toast"></div>

    <script src="${pageContext.request.contextPath}/asset/js/orderAdmin.js"></script>
    <script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
    </body>
    </html>
</c:if>
