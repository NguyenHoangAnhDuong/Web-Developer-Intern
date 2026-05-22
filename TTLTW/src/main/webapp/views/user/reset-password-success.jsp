<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt lại mật khẩu thành công</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset-password-success.css">
</head>
<body>
<div class="success-wrap">
    <div class="success-card">
        <div class="success-icon" aria-hidden="true">
            <i class="fa-solid fa-circle-check"></i>
        </div>
        <h2>Cập nhật mật khẩu thành công!</h2>
        <p class="subtitle">
            Mật khẩu của bạn đã được đổi.<br>
            Vui lòng đăng nhập lại bằng mật khẩu mới.
        </p>

        <div class="action-row">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/login">
                <i class="fa-solid fa-right-to-bracket"></i> Quay về đăng nhập
            </a>
            <a class="btn btn-outline" href="${pageContext.request.contextPath}/home">
                <i class="fa-solid fa-house"></i> Quay về trang chủ
            </a>
        </div>
    </div>
</div>
</body>
</html>
