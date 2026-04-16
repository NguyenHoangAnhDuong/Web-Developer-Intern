<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>Đăng Nhập</title>
            <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"
                rel="stylesheet" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/login.css">
        </head>

        <body>
            <div class="login">
                <div class="container">
                    <div class="inner-wrap">
                        <div class="title">
                            <h1>Đăng Nhập</h1>
                        </div>

                        <form id="login-form" action="login" method="post">

                            <!-- THÔNG BÁO LỖI -->
                            <div id="login-message" class="login-message ${error != null ? 'show error' : ''}">
                                <c:if test="${not empty error}">
                                    <i class="fa fa-warning"></i> ${error}
                                </c:if>
                                <i class="fa fa-warning"></i> ${message}
                            </div>


                            <!-- USERNAME / EMAIL -->
                            <div class="form-group-one">
                                <i class="fa fa-user icon-left"></i>
                                <input type="text" id="login-username" name="input" class="form-control"
                                    placeholder="tên đăng nhập/email" value="${inputValue != null ? inputValue : ''}" />
                            </div>


                            <div class="form-group-one">
                                <i class="fa fa-lock icon-left"></i>
                                <input type="password" id="login-password" name="password" class="form-control"
                                    placeholder="mật khẩu" autocomplete="off" />
                                <i onclick="changeTypePass()" id="icon-eye" class="fa fa-eye icon-toggle-pass"></i>
                                <i onclick="changeTypePass()" id="icon-eye-slash" class="fa fa-eye-slash icon-toggle-pass" style="display:none;"></i>
                            </div>

                            <div class="form-group actions">
                                <button type="submit" id="btn-login" class="btn">Đăng nhập</button>
                                <a href="forgorpassword.html" id="link-forgot-password" class="text-right">Quên mật
                                    khẩu?</a>
                            </div>

                            <div class="form-group social">
                                <label>Đăng nhập bằng:</label>
                                <div class="social-icons">
                                    <a href="${pageContext.request.contextPath}/login-facebook" id="social-facebook">
                                        <i class="fa fa-facebook"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/login-google" id="social-google"><i class="fa fa-google-plus"></i></a>
                                </div>
                            </div>
                        </form>

                        <div class="create-account">
                            <a href="register" id="link-create-account">
                                Tạo tài khoản mới <i class="fa fa-arrow-circle-o-right"></i>
                            </a>
                        </div>

                    </div>
                </div>
            </div>

            <script src="${pageContext.request.contextPath}/asset/js/login.js"></script>
            <script>
                function changeTypePass() {
                    var password = document.getElementById('login-password');
                    var eyeIcon = document.getElementById('icon-eye');
                    var eyeSlashIcon = document.getElementById('icon-eye-slash');
                    if (password.type === 'password') {
                        password.type = 'text';
                        eyeIcon.style.display = 'none';
                        eyeSlashIcon.style.display = 'inline';
                    } else {
                        password.type = 'password';
                        eyeIcon.style.display = 'inline';
                        eyeSlashIcon.style.display = 'none';
                    }
                }
            </script>
        </body>

        </html>