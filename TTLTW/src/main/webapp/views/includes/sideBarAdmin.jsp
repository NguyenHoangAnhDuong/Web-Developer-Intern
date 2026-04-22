<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!-- Sidebar Admin -->
<aside class="sidebar" id="sidebar">
    <div class="brand">
        <button class="toggle-icon sidebar-toggle"  id="sidebarToggle" style="cursor: pointer">
            <i class="fa-solid fa-bars" style="font-size: 16px"></i>
        </button>
        <div>
            <div class="muted">Quản lý bán hàng</div>
        </div>
    </div>
    <div class="section-title">Điều hướng</div>
    <nav class="nav-section" id="nav-section">
        <ul class="nav-list primary-nav" id="nav">
            <li class="nav-item">
                <a class="nav-link" data-target="dashboard" href="${pageContext.request.contextPath}/admin/dashboard"
                ><span class="icon"><i class="fa-solid fa-chart-pie"></i></span>
                    <span class="nav-label">Tổng Quan</span></a
                >
                <span class="nav-tooltip">Tổng Quan</span>
            </li>
            <li class="nav-item">
                <a class="nav-link" data-target="products" href="${pageContext.request.contextPath}/admin/products"
                ><span class="icon"><i class="fa-solid fa-box-open"></i></span>
                    <span class="nav-label">Sản phẩm</span></a
                >
                <span class="nav-tooltip">Sản phẩm</span>
            </li>
            <li class="nav-item">
                <a class="nav-link" data-target="orders" href="${pageContext.request.contextPath}/admin/orders"
                ><span class="icon"><i class="fa-solid fa-receipt"></i></span> <span class="nav-label">Đơn
          hàng</span></a
                >
                <span class="nav-tooltip">Đơn hàng</span>
            </li>
            <li class="nav-item">
                <a class="nav-link" data-target="customers" href="${pageContext.request.contextPath}/admin/users"
                ><span class="icon"><i class="fa-solid fa-users"></i></span> <span class="nav-label">Khách hàng</span></a
                >
                <span class="nav-tooltip">Khách hàng</span>
            </li>
            <li class="nav-item">
                <a class="nav-link" data-target="promotions" href="${pageContext.request.contextPath}/admin/vouchers"
                ><span class="icon"><i class="fa-solid fa-tags"></i></span> <span class="nav-label">Khuyến
          mãi</span></a
                >
                <span class="nav-tooltip">Khuyến mãi</span>
            </li>
            <li class="nav-item">
                <a class="nav-link" data-target="promotions" href="${pageContext.request.contextPath}/admin/feedbacks"
                ><span class="icon"><i class="fa-solid fa-award"></i></span> <span class="nav-label">Đánh giá</span></a
                >
                <span class="nav-tooltip">Đánh giá</span>
            </li>
        </ul>
        <ul class="nav-list second-nav">
            <li class="nav-item">
                <a class="nav-link" data-target="logout" href="${pageContext.request.contextPath}/logout">
                <span class="icon">
                <i class="fa-solid fa-right-from-bracket"></i></span>
                    <span class="nav-label">Đăng xuất</span>
                </a>
                <span class="nav-tooltip">Đăng xuất</span>
            </li>
        </ul>
    </nav>
</aside>

