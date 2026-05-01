<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng quan - Admin</title>

    <!-- ICON -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css"
          integrity="sha512-2SwdPD6INVrV/lHTZbO2nodKhrnDdJK9/kg2XD1r9uGqPo1cUbujc+IYdlYdEErWNu69gVcYgdxlmVmzTWnetw=="
          crossorigin="anonymous" referrerpolicy="no-referrer"/>

    <!-- CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/dashboardAdmin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">

    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
<script>
    const revenueByDaysData = [
        <c:forEach var="item" items="${revenueByDays}" varStatus="status">
            {date: '${item.date}', revenue: ${item.revenue}}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];
    const revenueByCategoryData = [
        <c:forEach var="item" items="${revenueByCategory}" varStatus="status">
            {category: '${item.category}', revenue: ${item.revenue}}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];
    const topProductsData = [
        <c:forEach var="item" items="${topProducts}" varStatus="status">
            {product: '${item.product}', sold: ${item.sold}}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];
</script>
<div class="app">
    <%@ include file="/views/includes/sideBarAdmin.jsp" %>

    <div class="container">
        <main class="content">
            <!-- Topbar -->
            <div class="topbar">
                <div class="topbar-left">
                    <div>
                        <h2 class="page-title" id="pageTitle">Tổng Quan</h2>
                        <div class="muted hidden" id="pageDesc">
                            Tổng quan nhanh về cửa hàng và hiệu suất bán hàng
                        </div>
                    </div>
                </div>
                <div class="topbar-right">
                    <div class="muted hidden">Xin chào, Admin</div>
                </div>
            </div>

            <!-- Error Message -->
            <c:if test="${not empty error}">
                <div class="error-message" style="background-color: #fee; color: #c33; padding: 10px; margin-bottom: 15px; border-radius: 4px; border: 1px solid #fcc;">
                    <i class="fas fa-exclamation-circle"></i> ${error}
                </div>
            </c:if>

            <!-- Success Message -->
            <c:if test="${not empty success}">
                <div class="success-message" style="background-color: #efe; color: #3c3; padding: 10px; margin-bottom: 15px; border-radius: 4px; border: 1px solid #cfc;">
                    <i class="fas fa-check-circle"></i> ${success}
                </div>
            </c:if>

            <section id="pages">
                <div class="page card" id="page-dashboard">
                    <h3>Thống kê</h3>

                    <!-- Actions Row -->
                    <div class="actions-row">
                        <a href="${pageContext.request.contextPath}/admin/product/add">
                            <button id="dashAddProduct" class="btn btn-outline-accent">
                                <i class="fa-solid fa-plus"></i> Thêm sản phẩm
                            </button>
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/vouchers">
                            <button id="dashAddPromo" class="btn btn-outline-accent">
                                <i class="fa-solid fa-tags"></i> Thêm khuyến mãi
                            </button>
                        </a>
                    </div>

                    <!-- KPI Cards -->
                    <div class="grid kpi-grid">
                        <div class="card">
                            <strong>Doanh thu hôm nay</strong>
                            <div class="muted">
                                <fmt:formatNumber value="${todayRevenue != null ? todayRevenue : 0}" pattern="#,###"/> ₫
                            </div>
                        </div>
                        <div class="card">
                            <strong>Đơn hàng mới</strong>
                            <div class="muted">${newOrders != null ? newOrders : 0}</div>
                        </div>
                        <div class="card">
                            <strong>Số khách truy cập</strong>
                            <div class="muted">${visitors != null ? visitors : 0}</div>
                        </div>
                        <div class="card">
                            <strong>Sản phẩm hết hàng</strong>
                            <div class="muted">${outOfStock != null ? outOfStock : 0}</div>
                        </div>
                    </div>

                    <!-- Charts Grid -->
                    <div class="charts-grid">
                        <!-- Revenue Chart -->
                        <div id="chart-cart" class="chart-card card">
                            <div class="filter-container">
                                <div class="filter-time">
                                    <button class="filter-btn ${empty startDate && days == 7 ? 'active' : ''}"
                                            onclick="location.href='${pageContext.request.contextPath}/admin/dashboard?days=7'">
                                        7 ngày
                                    </button>
                                    <button class="filter-btn ${empty startDate && (days == 30 || empty days) ? 'active' : ''}"
                                            onclick="location.href='${pageContext.request.contextPath}/admin/dashboard?days=30'">
                                        30 ngày
                                    </button>
                                    <button class="filter-btn ${empty startDate && days == 90 ? 'active' : ''}"
                                            onclick="location.href='${pageContext.request.contextPath}/admin/dashboard?days=90'">
                                        90 ngày
                                    </button>
                                </div>
                                <div class="date-filter">
                                    <form action="${pageContext.request.contextPath}/admin/dashboard" method="GET" class="date-form">
                                        <label for="startDate">Từ ngày:</label>
                                        <input type="date" id="startDate" name="startDate" value="${startDate}" required>
                                        <label for="endDate">Đến ngày:</label>
                                        <input type="date" id="endDate" name="endDate" value="${endDate}" required>
                                        <button type="submit" class="filter-btn ${not empty startDate ? 'active' : ''}">Lọc</button>
                                    </form>
                                </div>
                            </div>
                            <strong>Doanh thu theo ngày <c:choose>
                                <c:when test="${not empty startDate && not empty endDate}">(${startDate} - ${endDate})</c:when>
                                <c:otherwise>(${days != null ? days : 30} ngày)</c:otherwise>
                            </c:choose></strong>
                            <canvas id="salesLine" width="400" height="200"></canvas>
                        </div>

                        <div class="right-column">
                            <!-- Category Revenue Chart -->
                            <div class="chart-card card">
                                <strong>Doanh thu theo danh mục</strong>
                                <canvas id="categoryPie" width="400" height="200"></canvas>
                            </div>

                            <!-- Top Products Chart -->
                            <div class="chart-card card">
                                <strong>Sản phẩm bán chạy</strong>
                                <canvas id="topProductsBar" width="400" height="200"></canvas>
                            </div>

                            <!-- Recent Users -->
                            <div class="chart-card card">
                                <strong>5 người dùng mới</strong>
                                <div class="users-list">
                                    <c:choose>
                                        <c:when test="${not empty recentUsers}">
                                            <c:forEach var="user" items="${recentUsers}" varStatus="status">
                                                <div class="user-item ${status.last ? 'last' : ''}">
                                                    <div class="avatar variant-${(status.index % 5) + 1}">
                                                            ${user.initials}
                                                    </div>
                                                    <div>
                                                        <div class="user-name">${user.fullName}</div>
                                                        <div class="muted user-meta">
                                                                ${user.email} · Đăng ký ${user.daysAgo} ngày trước
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="user-item">
                                                <div class="avatar variant-1">NT</div>
                                                <div>
                                                    <div class="user-name">Nguyễn Minh Trí</div>
                                                    <div class="muted user-meta">
                                                        minhtri@example.com · Đăng ký 2 ngày trước
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="user-item">
                                                <div class="avatar variant-2">LH</div>
                                                <div>
                                                    <div class="user-name">Lê Hương</div>
                                                    <div class="muted user-meta">
                                                        lehuong@example.com · Đăng ký 3 ngày trước
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="user-item">
                                                <div class="avatar variant-3">TV</div>
                                                <div>
                                                    <div class="user-name">Trần Văn A</div>
                                                    <div class="muted user-meta">
                                                        tranva@example.com · Đăng ký 4 ngày trước
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="user-item">
                                                <div class="avatar variant-4">PB</div>
                                                <div>
                                                    <div class="user-name">Phạm Thị B</div>
                                                    <div class="muted user-meta">
                                                        phamtb@example.com · Đăng ký 5 ngày trước
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="user-item last">
                                                <div class="avatar variant-5">HA</div>
                                                <div>
                                                    <div class="user-name">Hoàng An</div>
                                                    <div class="muted user-meta">
                                                        hoangan@example.com · Đăng ký 7 ngày trước
                                                    </div>
                                                </div>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="view-all-wrap">
                                    <button id="viewAllUsers" class="btn btn-outline-accent">
                                        <a href="${pageContext.request.contextPath}/admin/users"
                                           style="color: white;">
                                            Xem tất cả
                                        </a>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </main>
    </div>
</div>

<!-- TOAST -->
<div id="toast"></div>

<!-- JS -->
<script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/dashboardAdmin.js"></script>
</body>
</html>