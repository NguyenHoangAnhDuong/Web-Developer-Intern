<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css" rel="stylesheet" />
    <title>Tạo tài khoản</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/register.css">
</head>
<body>
<div id="register-form">
    <form action="register"  method="post"class="form-regis">
        <h2>Đăng ký tài khoản</h2>
        <div class="row">
            <div class="input-group">
                <label for="fname">Họ</label>
                <input type="text" id="fname" name="fname" required>
            </div>
            <div class="input-group">
                <label for="lname">Tên</label>
                <input type="text" id="lname" name="lname" required>
            </div>
        </div>

        <div class="input-group">
            <label for="email">Email</label>
            <input type="email" id="email" name="email" required>
        </div>

        <div class="row">
            <div class="input-group">
                <label for="username">Tên đăng nhập</label>
                <input type="text" id="username" name="username" required>
            </div>
        </div>

        <div class="row">
            <div class="input-group">
                <label for="password">Mật khẩu</label>
                <input type="password"
                       id="password"
                       name="password" required
                >
                <i onclick="changeTypePass()" class="fa-regular fa-eye"></i>
                <i onclick="changeTypePass()" class="fa-regular fa-eye-slash"></i>
            </div>
            <div class="input-group">
                <label for="confirm">Xác nhận mật khẩu</label>
                <input type="password" id="confirm" name="confirm" required>
                <i class="fa-regular fa-eye" onclick="changeTypeConfPass()"></i>
                <i class="fa-regular fa-eye-slash" onclick="changeTypeConfPass()"></i>
            </div>
        </div>

        <div class="terms">
            <label>
                <input type="checkbox" required>
                Tôi đồng ý với <a href="#">Điều khoản dịch vụ</a> và <a href="#">Chính sách bảo mật</a>.
            </label>
        </div>

        <button type="submit">Tạo tài khoản</button>
        <div class="error-message" id="error-message">
            <c:if test="${not empty error}">${error}</c:if>
        </div>
        <div class="links">
            Đã có tài khoản? <a href="login">Đăng nhập</a>
        </div>
    </form>
</div>
</body>
<script src="${pageContext.request.contextPath}/asset/js/register.js"></script>
<script>
    function changeTypePass(){
        let password = document.getElementById('password');
        password.type = password.type === 'text' ? 'password' : 'text';
    }
    function changeTypeConfPass(){
        let password = document.getElementById('confirm');
        password.type = password.type === 'text' ? 'password' : 'text';
    }
</script>
</html>