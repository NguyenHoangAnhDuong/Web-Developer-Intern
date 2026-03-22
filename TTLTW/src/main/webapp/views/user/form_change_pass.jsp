<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport"
        content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="ie=edge">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css" rel="stylesheet" />
  <link rel="stylesheet" href=".${pageContext.request.contextPath}/asset/css/reset.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/formChangepass.css">
  <title>Form Đổi mật khẩu</title>
</head>
<body>
<%@ include file="/views/includes/toast.jsp" %>
<div class="container" id="reset-step" style="margin: 50px auto">
  <h2>Đặt mật khẩu mới</h2>
  <p>Tạo mật khẩu mới. Đảm bảo mật khẩu khác với mật khẩu trước đó để bảo mật.</p>
  <form id="reset-form" action="${pageContext.request.contextPath}/user/change-password" method="post">
    <div class="input-group">
      <label>Mật khẩu cũ</label>
      <div class="password-wrapper">
        <input type="password" name="password" id="old-pass" placeholder="Nhập mật khẩu cũ">
        <span class="toggle">
                <i class="fa-regular fa-eye"></i>
                <i class="fa-regular fa-eye-slash"></i>
            </span>
      </div>
    </div>
    <div class="input-group">
      <label>Mật khẩu mới</label>
      <div class="password-wrapper">
        <input type="password" name="new_password" id="new-pass" placeholder="Nhập mật khẩu mới">
        <span class="toggle">
                <i class="fa-regular fa-eye"></i>
                <i class="fa-regular fa-eye-slash"></i>
            </span>
      </div>
        <small class="hint">
            Ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&...).
        </small>
    </div>

    <div class="input-group">
      <label>Xác nhận mật khẩu</label>
      <div class="password-wrapper">
        <input type="password" name="confirm_password" id="confirm-pass" placeholder="Nhập lại mật khẩu">
        <span class="toggle">
                <i class="fa-regular fa-eye"></i>
                <i class="fa-regular fa-eye-slash"></i>
            </span>
      </div>
    </div>

    <button class="btn" id="update-btn" type="submit">Cập nhật mật khẩu</button>

    <c:if test="${not empty error}">
      <script>
        <%
          String error = (String) request.getAttribute("error");
          if (error != null) {
            session.setAttribute("toastMessage", error);
            session.setAttribute("toastType", "error");
          }
        %>
      </script>
    </c:if>

    <c:if test="${not empty message}">
      <script>
        <%
          String message = (String) request.getAttribute("message");
          if (message != null) {
            session.setAttribute("toastMessage", message);
            session.setAttribute("toastType", "success");
          }
        %>
      </script>
    </c:if>
  </form>
</div>
</body>
<jsp:include page="/views/includes/toast.jsp"/>
<script src="${pageContext.request.contextPath}/asset/js/formChangepass.js"></script>
</html>
