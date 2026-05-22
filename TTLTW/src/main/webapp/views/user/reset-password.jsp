<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt lại mật khẩu</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/formChangepass.css">
</head>
<body style="display:flex;align-items:center;justify-content:center;min-height:100vh;background:var(--color-bg);">

<div class="container" id="reset-step">
    <h2>Đặt mật khẩu mới</h2>
    <p>Hãy chọn mật khẩu mới khác với mật khẩu cũ để bảo mật.</p>

    <c:if test="${not empty error}">
        <div class="error-message"
             style="background:#fdecea;color:#b3261e;border:1px solid #f5c2c0;border-radius:4px;padding:10px 12px;margin-bottom:16px;text-align:left;font-size:13px;">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <form id="reset-form" action="${pageContext.request.contextPath}/reset-password" method="post" novalidate>
        <div class="input-group">
            <label>Mật khẩu mới</label>
            <div class="password-wrapper">
                <input type="password" name="password" id="new-pass" placeholder="Nhập mật khẩu mới">
                <span class="toggle">
                    <i class="fa-regular fa-eye"></i>
                    <i class="fa-regular fa-eye-slash"></i>
                </span>
            </div>
            <small class="hint">
                Ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&).
            </small>
        </div>

        <div class="input-group">
            <label>Xác nhận mật khẩu</label>
            <div class="password-wrapper">
                <input type="password" name="confirm" id="confirm-pass" placeholder="Nhập lại mật khẩu mới">
                <span class="toggle">
                    <i class="fa-regular fa-eye"></i>
                    <i class="fa-regular fa-eye-slash"></i>
                </span>
            </div>
        </div>

        <button class="btn" id="update-btn" type="submit">Xác nhận</button>
    </form>
</div>

<%@ include file="/views/includes/toast.jsp" %>
<script src="${pageContext.request.contextPath}/asset/js/reset-password.js"></script>
</body>
</html>
