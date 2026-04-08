<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="vn.edu.hcmuaf.fit.ttltw.model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Địa chỉ của tôi</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/accountSidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/info-user.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/addresses.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/notification.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/validation.css">
</head>

<body>
<jsp:include page="/views/includes/header.jsp"/>
<div id="pageWrapper">

    <jsp:include page="/views/includes/sidebarUser.jsp"/>

    <div class="address-content">
        <div class="address-header">
            <h1 class="address-title">Địa chỉ của tôi</h1>
            <button class="btn-add-address" id="btnAddAddress">
                <i class="fa-solid fa-plus"></i> Thêm địa chỉ mới
            </button>
        </div>

        <div class="address-section">
            <h2 class="section-title">Địa chỉ</h2>

            <div id="addressList" class="address-list">

                <!-- CÓ ĐỊA CHỈ -->
                <c:if test="${not empty addresses}">
                    <c:forEach var="a" items="${addresses}">
                        <div class="address-item ${a.status == 1 ? 'default' : ''}" data-id="${a.id}">
                            <div class="address-info">
                                <div class="address-row">
                                    <span class="address-name">${a.name}</span> |
                                    <span class="address-phone">${a.phoneNumber}</span>
                                </div>
                                <div class="address-details">${a.address}</div>
                                <c:if test="${a.status == 1}">
                                    <span class="address-default-badge">Mặc định</span>
                                </c:if>
                            </div>
                            <div class="address-actions">
                                <a href="javascript:void(0)" data-action="update" data-id="${a.id}">
                                    <i class="fa-solid fa-pen-to-square"></i> Sửa
                                </a>
                                <a href="javascript:void(0)" data-action="delete" data-id="${a.id}">
                                    <i class="fa-solid fa-trash"></i> Xóa
                                </a>

                                <c:if test="${a.status != 1}">
                                    <button data-action="set-default" data-id="${a.id}">
                                        <i class="fa-solid fa-check"></i> Đặt mặc định
                                    </button>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </c:if>

                <!-- CHƯA CÓ ĐỊA CHỈ -->
                <c:if test="${empty addresses}">
                    <div class="address-empty">
                        <i class="fa-solid fa-map-location-dot" style="font-size: 48px; color: #ccc; margin-bottom: 16px;"></i>
                        <p>Bạn chưa có địa chỉ nào. Hãy thêm địa chỉ mới.</p>
                    </div>
                </c:if>

            </div>
        </div>
    </div>
</div>

<!-- MODAL ADD ADDRESS -->
<div class="modal-overlay" id="modalOverlay">
    <div class="modal-content">
        <h2 class="modal-title">
            <i class="fa-solid fa-location-dot"></i>
            <span id="modalTitleText">Địa chỉ mới</span>
        </h2>

        <form class="address-form" id="addressForm">
            <input type="hidden" id="addressId">

            <div class="form-row">
                <label class="form-label">
                    <i class="fa-solid fa-user"></i> Họ và tên
                </label>
                <input type="text" id="name" class="form-input"
                       placeholder="Nhập họ và tên" required>
            </div>

            <div class="form-row">
                <label class="form-label">
                    <i class="fa-solid fa-phone"></i> Số điện thoại
                </label>
                <input type="text" id="phoneNumber" class="form-input"
                       placeholder="Nhập số điện thoại"
                       pattern="[0-9]{10}"
                       title="Số điện thoại phải gồm 10 chữ số"
                       required>
            </div>

            <div class="form-row">
                <label>Tỉnh / Thành phố</label>
                <input type="text" id="provinceInput" class="form-input" autocomplete="off">
                <div id="provinceList" class="suggest-list"></div>
            </div>

            <div class="form-row">
                <label>Quận / Huyện</label>
                <input type="text" id="districtInput" class="form-input" autocomplete="off">
                <div id="districtList" class="suggest-list"></div>
            </div>

            <div class="form-row">
                <label>Phường / Xã</label>
                <input type="text" id="wardInput" class="form-input" autocomplete="off">
                <div id="wardList" class="suggest-list"></div>
            </div>

            <div class="form-row form-row--full">
                <label class="form-label">Địa chỉ chi tiết</label>
                <input type="text" id="detailAddress" class="form-input address-details"
                       placeholder="Số nhà, tên đường">
            </div>

            <div class="form-row form-row--full">
                <label class="checkbox-label">
                    <input type="checkbox" id="status">
                    <span>Đặt làm địa chỉ mặc định</span>
                </label>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn-back" id="btnBack">
                    <i class="fa-solid fa-xmark"></i> Hủy
                </button>
                <button type="submit" class="btn-complete">
                    <i class="fa-solid fa-check"></i> Hoàn thành
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    window.contextPath = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/asset/js/notification.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/addresses.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/info-user.js"></script>
</body>
</html>