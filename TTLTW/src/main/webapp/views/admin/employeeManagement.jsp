<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý nhân viên</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/userManagement.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/toast.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/employeeManagement.css">
</head>
<body>
<div class="app">
    <%@ include file="/views/includes/sideBarAdmin.jsp" %>
    <div class="container" id="container-user-management">
        <div class="topbar">
            <div class="topbar-left">
                <h2 class="page-title">Quản lý nhân viên</h2>
            </div>
            <div>
                <button class="btn-add" id="btn-open-create">
                    <i class="fa-solid fa-plus"></i> Tạo nhân viên
                </button>
            </div>
        </div>

        <div class="header" id="filter-bar">
            <form method="get" action="${pageContext.request.contextPath}/admin/employees" style="display:flex; gap:8px; width:100%;">
                <input type="text" name="search" placeholder="Tìm theo tên đăng nhập, email, họ tên..."
                       value="${currentSearch}" style="flex:1;">
                <button class="btn btn-primary" type="submit">Tìm</button>
            </form>
        </div>

        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Họ tên</th>
                <th>Tên đăng nhập</th>
                <th>Email</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty employees}">
                    <tr><td colspan="7" style="text-align:center; padding:40px; color:#666;">Chưa có nhân viên nào</td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="e" items="${employees}">
                        <tr data-id="${e.id}">
                            <td>${e.id}</td>
                            <td>${e.firstName} ${e.lastName}</td>
                            <td>${e.username}</td>
                            <td>${e.email}</td>
                            <td>
                                <select class="role-select" data-id="${e.id}">
                                    <c:forEach var="r" items="${roles}">
                                        <option value="${r.id}" ${r.id == e.rolesId ? 'selected' : ''}>${r.displayName}</option>
                                    </c:forEach>
                                </select>
                            </td>
                            <td>
                                <select class="status-select" data-id="${e.id}">
                                    <option value="1" ${e.status == 1 ? 'selected' : ''}>Hoạt động</option>
                                    <option value="0" ${e.status == 0 ? 'selected' : ''}>Tạm khoá</option>
                                </select>
                            </td>
                            <td>
                                <button class="btn btn-primary btn-save" data-id="${e.id}">Lưu</button>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>

        <div class="footer-user">
            <p>Tổng <strong>${total}</strong> nhân viên — Trang ${page}/${totalPage}</p>
            <c:if test="${totalPage > 1}">
                <div class="pagination">
                    <c:forEach begin="1" end="${totalPage}" var="i">
                        <c:choose>
                            <c:when test="${i == page}"><span class="page-link active">${i}</span></c:when>
                            <c:otherwise>
                                <a class="page-link" href="${pageContext.request.contextPath}/admin/employees?page=${i}&search=${currentSearch}">${i}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
            </c:if>
        </div>
    </div>
</div>

<!-- Modal tạo nhân viên -->
<div class="modal-overlay" id="create-modal">
    <div class="modal-box">
        <h3>Tạo tài khoản nhân viên</h3>
        <form id="create-employee-form">
            <label>Họ tên
                <input type="text" name="fullName" required>
            </label>
            <label>Tên đăng nhập
                <input type="text" name="username" required>
            </label>
            <label>Email
                <input type="email" name="email" required>
            </label>
            <label>Mật khẩu
                <input type="password" name="password" required>
            </label>
            <label>Vai trò
                <select name="rolesId" required>
                    <c:forEach var="r" items="${roles}">
                        <option value="${r.id}">${r.displayName}</option>
                    </c:forEach>
                </select>
            </label>
            <div class="msg-error" id="create-error"></div>
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" id="btn-cancel-create">Huỷ</button>
                <button type="submit" class="btn btn-primary">Tạo</button>
            </div>
        </form>
    </div>
</div>

<script>
    const CTX = '${pageContext.request.contextPath}';

    document.getElementById('btn-open-create').onclick = () => {
        document.getElementById('create-modal').classList.add('show');
    };
    document.getElementById('btn-cancel-create').onclick = () => {
        document.getElementById('create-modal').classList.remove('show');
        document.getElementById('create-error').textContent = '';
    };

    document.getElementById('create-employee-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        fd.append('action', 'create');
        const res = await fetch(CTX + '/admin/employees', { method:'POST', body: new URLSearchParams(fd) });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Đã tạo nhân viên', 'success');
            setTimeout(() => location.reload(), 800);
        } else {
            document.getElementById('create-error').textContent = data.message || 'Có lỗi xảy ra';
            showToast(data.message || 'Tạo nhân viên thất bại', 'error');
        }
    });

    document.querySelectorAll('.btn-save').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.dataset.id;
            const row = btn.closest('tr');
            const rolesId = row.querySelector('.role-select').value;
            const status = row.querySelector('.status-select').value;
            const params = new URLSearchParams({ action:'update', id, rolesId, status });
            const res = await fetch(CTX + '/admin/employees', { method:'POST', body: params });
            const data = await res.json();
            showToast(data.message, data.success ? 'success' : 'error');
        });
    });
</script>
<%@ include file="/views/includes/toast.jsp" %>
<script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
</body>
</html>
