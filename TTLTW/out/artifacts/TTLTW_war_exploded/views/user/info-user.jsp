<%@ page import="vn.edu.hcmuaf.fit.ttltw.model.User" %>
<%@ page import="vn.edu.hcmuaf.fit.ttltw.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Dữ liệu được set bởi InfoUserServlet
    vn.edu.hcmuaf.fit.ttltw.model.User user = (User) request.getAttribute("user");
    String avatarPath = (String) request.getAttribute("avatarPath");
%>
<html>
<head>
    <title>Hồ Sơ Người Dùng</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/accountSidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/info-user.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/notification.css">
</head>

<body>
<jsp:include page="/views/includes/header.jsp"/>
<div id="pageWrapper">

    <jsp:include page="/views/includes/sidebarUser.jsp"/>

    <div class="info-user">
        <div class="container">

            <div class="title">
                <i class="fa-solid fa-user-circle"></i>
                <span>Hồ Sơ Của Tôi</span>
                <p class="subtitle">Quản lý thông tin cá nhân của bạn</p>
            </div>

            <div class="profile">

                <%-- FORM 1: UPDATE PROFILE --%>
                <form id="update-form" class="left"
                      method="POST"
                      action="${pageContext.request.contextPath}/user/profile">

                    <input type="hidden" name="action" value="update">

                    <div class="row">
                        <label class="label">
                            <i class="fa-solid fa-user"></i> Tên đăng nhập
                        </label>
                        <div class="readonly">
                            <%= user != null ? user.getUsername() : "" %>
                        </div>
                    </div>

                    <div class="row">
                        <label for="firstName" class="label">
                            <i class="fa-solid fa-id-card"></i> Họ <span class="required">*</span>
                        </label>
                        <input type="text" id="firstName" name="firstName" class="input"
                               value="<%= user != null && user.getFirstName() != null ? user.getFirstName() : "" %>"
                               placeholder="Nhập họ của bạn"
                               required
                               disabled>
                    </div>

                    <div class="row">
                        <label for="lastName" class="label">
                            <i class="fa-solid fa-id-card"></i> Tên <span class="required">*</span>
                        </label>
                        <input type="text" id="lastName" name="lastName" class="input"
                               value="<%= user != null && user.getLastName() != null ? user.getLastName() : "" %>"
                               placeholder="Nhập tên của bạn"
                               disabled
                               required>
                    </div>

                    <div class="row">
                        <label for="emailInput" class="label">
                            <i class="fa-solid fa-envelope"></i> Email
                        </label>
                        <input type="email" id="emailInput" name="email" class="input readonly"
                               value="<%= user != null && user.getEmail() != null ? user.getEmail() : "" %>"
                               placeholder="example@email.com"
                               disabled>
                        <span class="error" id="emailError"></span>
                    </div>

                    <div class="buttons">
                        <button type="button" class="btn primary" id="btn-edit">
                            <i class="fa-solid fa-pen-to-square"></i> Chỉnh sửa
                        </button>
                        <button type="button" class="btn outline" id="btn-logout">
                            <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
                        </button>
                        <button type="submit" class="btn primary hidden" id="btn-save">
                            <i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi
                        </button>
                    </div>
                </form>

                <form id="avatar-form" class="right"
                      method="POST"
                      action="${pageContext.request.contextPath}/user/profile"
                      enctype="multipart/form-data">

                    <input type="hidden" name="action" value="upload-avatar">

                    <div class="avatar-container">
                        <div class="avatar">
                            <img id="user-avatar"
                                 src="<%= avatarPath %>"
                                 alt="User Avatar"
                                 class="avatar-img"/>
                        </div>
                    </div>

                    <label for="avatarInput" class="btn small outline">
                        <i class="fa-solid fa-camera"></i> Chọn Ảnh
                    </label>

                    <p class="avatar-note">
                        <i class="fa-solid fa-circle-info"></i>
                        Dung lượng tối đa 5MB<br>
                        Định dạng: JPG, PNG, GIF
                    </p>

                    <input id="avatarInput"
                           name="avatar"
                           type="file"
                           accept="image/png, image/jpeg, image/jpg, image/gif"
                           class="hidden-input">
                </form>

            </div>
        </div>
    </div>
</div>

<script>
    window.contextPath = '${pageContext.request.contextPath}';
</script>
<jsp:include page="/views/includes/toast.jsp"/>
<script src="${pageContext.request.contextPath}/asset/js/notification.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/info-user.js"></script>
</body>
</html>