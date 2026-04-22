<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý vai trò &amp; quyền</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/userManagement.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/roleManagement.css">
</head>
<body>
<div class="app">
    <%@ include file="/views/includes/sideBarAdmin.jsp" %>
    <div class="container">
        <div class="topbar">
            <h2 class="page-title">Vai trò &amp; phân quyền</h2>
        </div>

        <div class="role-layout">
            <!-- DANH SÁCH VAI TRÒ -->
            <div class="role-list">
                <h3 style="margin:0 0 12px;">Vai trò</h3>
                <div id="role-items">
                    <c:forEach var="r" items="${roles}">
                        <div class="role-item" data-id="${r.id}" data-name="${r.name}"
                             data-system="${r.system}"
                             data-display="${r.displayName}"
                             data-desc="${r.description}">
                            <span>${r.displayName}</span>
                            <c:if test="${r.system}"><span class="sys-badge">Hệ thống</span></c:if>
                        </div>
                    </c:forEach>
                </div>
                <button class="btn-add-role" id="btn-new-role">
                    <i class="fa-solid fa-plus"></i> Thêm vai trò mới
                </button>
            </div>

            <!-- CHI TIẾT VAI TRÒ -->
            <div class="role-detail">
                <form id="role-form">
                    <input type="hidden" name="id" id="role-id">
                    <div class="form-row">
                        <div>
                            <label>Mã vai trò (tên hệ thống)</label>
                            <input type="text" name="name" id="role-name" placeholder="vd: sales_staff" required>
                        </div>
                        <div>
                            <label>Tên hiển thị</label>
                            <input type="text" name="displayName" id="role-display" required>
                        </div>
                    </div>
                    <div class="form-row">
                        <div>
                            <label>Mô tả</label>
                            <input type="text" name="description" id="role-desc">
                        </div>
                    </div>

                    <h3 style="margin:16px 0 10px; font-size:16px;">Phân quyền</h3>
                    <c:forEach var="entry" items="${permissionsByModule}">
                        <div class="module-group">
                            <h4>${entry.key}</h4>
                            <div class="perm-grid">
                                <c:forEach var="p" items="${entry.value}">
                                    <label>
                                        <input type="checkbox" name="permissionIds" value="${p.id}" data-perm="${p.id}">
                                        <span>${p.displayName}</span>
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                    </c:forEach>

                    <div class="actions-bar">
                        <button type="button" class="btn btn-danger" id="btn-delete">Xoá</button>
                        <button type="submit" class="btn btn-primary" id="btn-save">Lưu</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    const CTX = '${pageContext.request.contextPath}';
    let mode = 'create'; // 'create' | 'update'
    let currentRole = null;

    const $items = document.getElementById('role-items');
    const $id = document.getElementById('role-id');
    const $name = document.getElementById('role-name');
    const $display = document.getElementById('role-display');
    const $desc = document.getElementById('role-desc');

    function clearPerms() {
        document.querySelectorAll('input[name="permissionIds"]').forEach(c => c.checked = false);
    }
    function setPerms(ids) {
        clearPerms();
        ids.forEach(id => {
            const el = document.querySelector('input[name="permissionIds"][value="' + id + '"]');
            if (el) el.checked = true;
        });
    }

    function toCreate() {
        mode = 'create';
        currentRole = null;
        $id.value = '';
        $name.value = '';
        $name.readOnly = false;
        $display.value = '';
        $desc.value = '';
        clearPerms();
        document.getElementById('btn-delete').style.display = 'none';
        document.querySelectorAll('.role-item').forEach(i => i.classList.remove('active'));
    }

    async function toUpdate(item) {
        mode = 'update';
        currentRole = item.dataset;
        $id.value = item.dataset.id;
        $name.value = item.dataset.name;
        $name.readOnly = true;
        $display.value = item.dataset.display;
        $desc.value = item.dataset.desc === 'null' ? '' : item.dataset.desc;
        document.querySelectorAll('.role-item').forEach(i => i.classList.remove('active'));
        item.classList.add('active');
        document.getElementById('btn-delete').style.display =
            item.dataset.system === 'true' ? 'none' : 'inline-block';
        const res = await fetch(CTX + '/admin/roles?action=permissions&roleId=' + item.dataset.id);
        const ids = await res.json();
        setPerms(ids);
    }

    document.querySelectorAll('.role-item').forEach(item => {
        item.addEventListener('click', () => toUpdate(item));
    });
    document.getElementById('btn-new-role').onclick = toCreate;

    document.getElementById('role-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        fd.append('action', mode);
        const res = await fetch(CTX + '/admin/roles', { method:'POST', body: new URLSearchParams(fd) });
        const data = await res.json();
        showToast(data.message || (data.success ? 'Đã lưu' : 'Thất bại'),
                  data.success ? 'success' : 'error');
        if (data.success) setTimeout(() => location.reload(), 800);
    });

    document.getElementById('btn-delete').addEventListener('click', async () => {
        if (!$id.value) return;
        if (!confirm('Xoá vai trò này?')) return;
        const params = new URLSearchParams({ action:'delete', id: $id.value });
        const res = await fetch(CTX + '/admin/roles', { method:'POST', body: params });
        const data = await res.json();
        showToast(data.message, data.success ? 'success' : 'error');
        if (data.success) setTimeout(() => location.reload(), 800);
    });

    toCreate();
</script>
<%@ include file="/views/includes/toast.jsp" %>
<script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
</body>
</html>
