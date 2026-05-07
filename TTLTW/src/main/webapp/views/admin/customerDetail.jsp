<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isSuper" value="${sessionScope.roleName == 'super_admin'}" />
<c:set var="perms" value="${sessionScope.permissions}" />
<c:set var="canUpdateCustomer" value="${isSuper || (perms != null && perms.contains('customer.update'))}" />
<c:set var="canResetCustomerPwd" value="${isSuper || (perms != null && perms.contains('customer.reset_password'))}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết khách hàng</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/customerDetail.css">
</head>
<body>
<div class="app">
    <%@ include file="/views/includes/sideBarAdmin.jsp" %>

    <div class="container">

        <div class="detail-topbar">
            <a href="${pageContext.request.contextPath}/admin/users" class="btn-back">
                <i class="fa-solid fa-arrow-left"></i> Quay lại
            </a>
            <h2>Chi tiết khách hàng</h2>
        </div>

        <c:if test="${savedSuccess}">
            <div class="saved-notice">
                <i class="fa-solid fa-circle-check"></i>
                Thông tin khách hàng đã được lưu thành công.
            </div>
        </c:if>

        <c:if test="${passwordResetSuccess}">
            <div class="saved-notice">
                <i class="fa-solid fa-circle-check"></i>
                Mật khẩu mới đã được cập nhật thành công cho khách hàng.
            </div>
        </c:if>

        <c:if test="${not empty passwordResetError}">
            <div class="error-notice">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${passwordResetError}"/>
            </div>
        </c:if>

        <div class="detail-card">

            <!-- Avatar -->
            <div class="avatar-section">
                <c:choose>
                    <c:when test="${not empty customer.avatar}">
                        <c:choose>
                            <c:when test="${customer.avatar.startsWith('/')}">
                                <img src="${pageContext.request.contextPath}${customer.avatar}"
                                     alt="${customer.username}"
                                     onerror="this.src='${pageContext.request.contextPath}/assert/img/admin.jpg'">
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}/${customer.avatar}"
                                     alt="${customer.username}"
                                     onerror="this.src='${pageContext.request.contextPath}/assert/img/admin.jpg'">
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <img src="${pageContext.request.contextPath}/assert/img/admin.jpg"
                             alt="${customer.username}">
                    </c:otherwise>
                </c:choose>
                <span class="avatar-label">Ảnh đại diện</span>
            </div>

            <!-- Form -->
            <form class="detail-form" method="POST"
                  action="${pageContext.request.contextPath}/admin/customers/detail">
                <input type="hidden" name="id" value="${customer.id}">

                <div class="form-row">
                    <div class="form-group">
                        <label>ID</label>
                        <input type="text" value="${customer.id}" disabled>
                    </div>
                    <div class="form-group">
                        <label>Tên đăng nhập</label>
                        <input type="text" value="${customer.username}" disabled>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Họ</label>
                        <input type="text" name="firstName"
                               value="${customer.firstName}"
                               class="editable-input" disabled>
                    </div>
                    <div class="form-group">
                        <label>Tên</label>
                        <input type="text" name="lastName"
                               value="${customer.lastName}"
                               class="editable-input" disabled>
                    </div>
                </div>

                <div class="form-group">
                    <label>Email</label>
                    <input type="email" name="email"
                           value="${customer.email}"
                           class="editable-input" disabled>
                </div>

                <div class="form-group">
                    <label>Trạng thái</label>
                    <div>
                        <span class="status-badge ${customer.status == 1 ? 'status-active' : 'status-locked'}">
                            <c:choose>
                                <c:when test="${customer.status == 1}">Hoạt động</c:when>
                                <c:otherwise>Tạm khóa</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>

                <c:if test="${canUpdateCustomer}">
                    <div class="form-actions">
                        <button type="button" class="btn-update" id="update-btn">
                            <i class="fa-solid fa-pen"></i> Cập nhật
                        </button>
                        <button type="submit" class="btn-save" id="save-btn" style="display:none;">
                            <i class="fa-solid fa-floppy-disk"></i> Lưu
                        </button>
                    </div>
                </c:if>
            </form>

        </div>

        <!-- Đặt lại mật khẩu (dành cho admin/nhân viên hỗ trợ khách hàng quên mật khẩu) -->
        <c:if test="${canResetCustomerPwd}">
        <div class="password-reset-card">
            <div class="password-reset-header">
                <h3><i class="fa-solid fa-key"></i> Đặt lại mật khẩu</h3>
                <p>Sử dụng khi khách hàng quên mật khẩu và nhờ nhân viên hỗ trợ tạo mật khẩu mới.</p>
            </div>

            <button type="button" class="btn-reset-pwd" id="reset-pwd-btn">
                <i class="fa-solid fa-rotate-right"></i> Cập nhật mật khẩu mới
            </button>

            <form id="reset-pwd-form" class="reset-pwd-form" method="POST"
                  action="${pageContext.request.contextPath}/admin/customers/detail"
                  style="display:none;">
                <input type="hidden" name="id" value="${customer.id}">
                <input type="hidden" name="action" value="resetPassword">

                <div class="reset-pwd-row">
                    <div class="reset-pwd-input">
                        <input type="password" name="newPassword" id="newPassword"
                               placeholder="Nhập mật khẩu mới" autocomplete="new-password" required>
                        <button type="button" class="btn-toggle-pwd" id="toggle-pwd-btn"
                                aria-label="Hiện/ẩn mật khẩu">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                    <button type="submit" class="btn-confirm-pwd">
                        <i class="fa-solid fa-check"></i> Cập nhật
                    </button>
                    <button type="button" class="btn-cancel-pwd" id="cancel-pwd-btn">
                        <i class="fa-solid fa-xmark"></i> Hủy
                    </button>
                </div>
                <small class="pwd-hint">
                    Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.
                </small>
            </form>
        </div>
        </c:if>

    </div>
</div>

<script src="${pageContext.request.contextPath}/asset/js/customerDetail.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
</body>
</html>
