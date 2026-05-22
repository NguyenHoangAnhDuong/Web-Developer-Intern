<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên mật khẩu</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/forgotpass.css">
</head>
<body>
<div id="forgot-password">
    <div class="form-box">
        <h2>Quên mật khẩu</h2>
        <p class="subtitle">
            Nhập email đã đăng ký, hệ thống sẽ gửi mã OTP để xác thực.
        </p>

        <c:if test="${not empty error}">
            <div class="error-message"><c:out value="${error}"/></div>
        </c:if>

        <form id="forgot-form" action="${pageContext.request.contextPath}/forgot-password" method="post" novalidate>
            <div class="input-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email"
                       placeholder="Nhập email của bạn"
                       value="<c:out value='${emailValue}'/>" required>
                <div class="field-error" data-for="email"></div>
            </div>

            <button type="submit">Gửi mã OTP</button>
        </form>

        <div class="links">
            <a href="${pageContext.request.contextPath}/login">
                <i class="fa-solid fa-arrow-left"></i> Quay lại đăng nhập
            </a>
        </div>
    </div>
</div>
<script src="${pageContext.request.contextPath}/asset/js/forgot-password.js"></script>
</body>
</html>
