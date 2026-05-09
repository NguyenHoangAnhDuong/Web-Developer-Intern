<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!-- Header -->
<header id="header" data-context-path="${pageContext.request.contextPath}">
    <div class="container">
        <div class="inner-wrap">
            <div class="logo" id="logo">
                <a href="${pageContext.request.contextPath}/home">

                    <img src="${pageContext.request.contextPath}/asset/img/logo.png" alt="Logo Website" id="logo-img">
                </a>
            </div>

            <nav class="menu" id="menu">
                <ul>
                    <li><a href="${pageContext.request.contextPath}/home" id="nav-home">Trang chủ</a></li>
                    <li><a href="${pageContext.request.contextPath}/listproduct" id="nav-phone">Điện thoại</a></li>
                    <li class="has-mega" id="nav-accessory-item">
                        <a href="${pageContext.request.contextPath}/listproduct_accessory" id="nav-accessory">Linh kiện</a>
                        <div class="mega-menu" id="mega-accessory">
                            <div class="mega-inner">
                                <div class="mega-col">
                                    <h4>Linh kiện di động</h4>
                                    <c:url var="manHinhUrl" value="/views/user/listproduct_accessory.jsp">
                                        <c:param name="categoryName" value="Màn hình "/>
                                    </c:url>
                                    <a href="${manHinhUrl}">Màn hình</a>
                                    
                                    <c:url var="pinUrl" value="/views/user/listproduct_accessory.jsp">
                                        <c:param name="categoryName" value="Pin"/>
                                    </c:url>
                                    <a href="${pinUrl}">Pin</a>
                                    
                                    <c:url var="cameraUrl" value="/views/user/listproduct_accessory.jsp">
                                        <c:param name="categoryName" value="Camera"/>
                                    </c:url>
                                    <a href="${cameraUrl}">Camera</a>
                                </div>
                                <div class="mega-col">
                                    <h4>&nbsp;</h4>
                                    <c:url var="loaMicUrl" value="/views/user/listproduct_accessory.jsp">
                                        <c:param name="categoryName" value="Loa / Mic"/>
                                    </c:url>
                                    <a href="${loaMicUrl}">Loa / Mic</a>
                                    
                                    <c:url var="capSacUrl" value="/views/user/listproduct_accessory.jsp">
                                        <c:param name="categoryName" value="Cáp sạc / Cổng sạc"/>
                                    </c:url>
                                    <a href="${capSacUrl}">Cáp sạc / Cổng sạc</a>
                                    <c:url var="opLungUrl" value="/views/user/listproduct_accessory.jsp">
                                        <c:param name="categoryName" value="Vỏ và khung sườn"/>
                                    </c:url>
                                    <a href="${opLungUrl}">Vỏ và khung sườn</a>
                                </div>
                            </div>
                        </div>
                    </li>
                    <li><a href="${pageContext.request.contextPath}/home#footer" id="nav-contact">Liên hệ</a></li>
                </ul>
            </nav>

            <div class="box" id="icon-box">
                <ul>
                    <div class="search-item">
                        <a href="#" id="btn-search">
                            <i class="fa-solid fa-magnifying-glass"></i>
                        </a>
                        <input id="header-search" class="search-input" type="text" placeholder="Tìm kiếm sản phẩm..." />

                        <div class="search-suggest" id="search-suggest"></div>
                    </div>

                    <li class="cart-item">
                        <a href="${pageContext.request.contextPath}/cart" id="btn-cart">
                            <i class="fa-solid fa-cart-shopping"></i>
                            <span class="cart-badge" id="cart-badge">
                                <c:out value="${sessionScope.cartItemCount != null ? sessionScope.cartItemCount : 0}" />
                            </span>
                        </a>
                    </li>

                    <li id="user-area" class="user-area">
                        <div class="user-profile" id="user-profile">
                            <i class="fa-solid fa-user"></i>
                            <span class="username" id="header-username">
                                <c:choose>
                                    <c:when test="${not empty sessionScope.user}">
                                        ${sessionScope.user.username}
                                    </c:when>
                                    <c:otherwise>
                                        Đăng nhập
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>

                        <div class="user-dropdown" id="user-dropdown">
                            <c:choose>
                                <c:when test="${not empty sessionScope.user}">
                                    <a href="${pageContext.request.contextPath}/user/profile">Tài khoản của tôi</a>
                                    <a href="${pageContext.request.contextPath}/user/order">Đơn mua</a>
                                    <a href="${pageContext.request.contextPath}/logout" id="logout-link">Đăng xuất</a>
                                </c:when>
                            </c:choose>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</header>
<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>