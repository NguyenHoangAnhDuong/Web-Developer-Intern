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
    <form action="register" method="post" class="form-regis" novalidate>
        <h2>Đăng ký tài khoản</h2>

        <c:if test="${not empty errors.general}">
            <div class="error-message general-error">${errors.general}</div>
        </c:if>

        <div class="row">
            <div class="input-group ${not empty errors.fname ? 'has-error' : ''}">
                <label for="fname">Họ</label>
                <input type="text" id="fname" name="fname"
                       value="<c:out value='${form.fname}'/>" required>
                <div class="field-error" data-for="fname">
                    <c:if test="${not empty errors.fname}">${errors.fname}</c:if>
                </div>
            </div>
            <div class="input-group ${not empty errors.lname ? 'has-error' : ''}">
                <label for="lname">Tên</label>
                <input type="text" id="lname" name="lname"
                       value="<c:out value='${form.lname}'/>" required>
                <div class="field-error" data-for="lname">
                    <c:if test="${not empty errors.lname}">${errors.lname}</c:if>
                </div>
            </div>
        </div>

        <div class="input-group ${not empty errors.email ? 'has-error' : ''}">
            <label for="email">Email</label>
            <input type="email" id="email" name="email"
                   value="<c:out value='${form.email}'/>" required>
            <div class="field-error" data-for="email">
                <c:if test="${not empty errors.email}">${errors.email}</c:if>
            </div>
        </div>

        <div class="row">
            <div class="input-group ${not empty errors.username ? 'has-error' : ''}">
                <label for="username">Tên đăng nhập</label>
                <input type="text" id="username" name="username"
                       value="<c:out value='${form.username}'/>" required>
                <div class="field-error" data-for="username">
                    <c:if test="${not empty errors.username}">${errors.username}</c:if>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="input-group ${not empty errors.password ? 'has-error' : ''}">
                <label for="password">Mật khẩu</label>
                <div class="pw-wrap">
                    <input type="password" id="password" name="password" required>
                    <i onclick="changeTypePass()" class="fa-regular fa-eye"></i>
                    <i onclick="changeTypePass()" class="fa-regular fa-eye-slash"></i>
                </div>
                <div class="field-error" data-for="password">
                    <c:if test="${not empty errors.password}">${errors.password}</c:if>
                </div>
            </div>
            <div class="input-group ${not empty errors.confirm ? 'has-error' : ''}">
                <label for="confirm">Xác nhận mật khẩu</label>
                <div class="pw-wrap">
                    <input type="password" id="confirm" name="confirm" required>
                    <i class="fa-regular fa-eye" onclick="changeTypeConfPass()"></i>
                    <i class="fa-regular fa-eye-slash" onclick="changeTypeConfPass()"></i>
                </div>
                <div class="field-error" data-for="confirm">
                    <c:if test="${not empty errors.confirm}">${errors.confirm}</c:if>
                </div>
            </div>
        </div>

        <div class="terms ${not empty errors.terms ? 'has-error' : ''}">
            <label>
                <input type="checkbox" name="terms" required>
                Tôi đồng ý với <a href="#">Điều khoản dịch vụ</a> và <a href="#">Chính sách bảo mật</a>.
            </label>
            <div class="field-error" data-for="terms">
                <c:if test="${not empty errors.terms}">${errors.terms}</c:if>
            </div>
        </div>

        <button type="submit">Tạo tài khoản</button>

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
