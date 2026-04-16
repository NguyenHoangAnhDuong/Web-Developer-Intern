<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý khách hàng</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/userManagement.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/toast.css">
</head>
<body>
<div class="app">
    <%@ include file="/views/includes/sideBarAdmin.jsp" %>

    <div class="container" id="container-user-management">

        <div class="topbar">
            <div class="topbar-left">
                <div>
                    <h2 class="page-title" id="title-user-management">Quản lý khách hàng</h2>
                </div>
            </div>
        </div>

        <div class="header" id="filter-bar">
            <!-- GIỮ GIÁ TRỊ TÌM KIẾM SAU KHI RELOAD -->
            <input type="text"
                   placeholder="Tìm kiếm theo ID, username, email..."
                   id="search-input"
                   value="${currentSearch}">

            <select id="status-filter">
                <option value="">Trạng thái</option>
                <option value="Hoạt động" ${currentStatus == 'Hoạt động' ? 'selected' : ''}>Hoạt động</option>
                <option value="Tạm khóa" ${currentStatus == 'Tạm khóa' ? 'selected' : ''}>Tạm khóa</option>
            </select>

            <!-- NÚT RESET FILTER -->
            <button id="reset-filter-btn" class="btn-reset" title="Reset bộ lọc">
                <i class="fa-solid fa-rotate-right"></i>
            </button>
        </div>

        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Ảnh đại diện</th>
                <th>Tên đăng nhập</th>
                <th>Email</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
            </tr>
            </thead>

            <tbody id="user-table-body">
            <c:choose>
                <c:when test="${empty users}">
                    <tr id="no-results-row">
                        <td colspan="6" style="text-align: center; padding: 40px; color: #666;">
                            <i class="fa-solid fa-search" style="font-size: 48px; color: #ddd; margin-bottom: 16px;"></i>
                            <p style="margin: 0; font-size: 16px;">Không tìm thấy khách hàng nào</p>
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="u" items="${users}">
                        <tr data-id="${u.id}">

                            <!-- CỘT ID -->
                            <td class="id-cell">${u.id}</td>

                            <!-- CỘT AVATAR -->
                            <td class="avatar-cell">
                                <c:choose>
                                    <c:when test="${not empty u.avatar}">
                                        <c:choose>
                                            <c:when test="${u.avatar.startsWith('/')}">
                                                <img src="${pageContext.request.contextPath}${u.avatar}" style="width:40px;height:40px;border-radius:50%;"
                                                     alt="${u.username}"
                                                     onerror="this.src='${pageContext.request.contextPath}/assert/img/admin.jpg'">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}/${u.avatar}" style="width:40px;height:40px;border-radius:50%;"
                                                     alt="${u.username}"
                                                     onerror="this.src='${pageContext.request.contextPath}/assert/img/admin.jpg'">
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}/assert/img/admin.jpg" style="width:40px;height:40px;border-radius:50%;"
                                             alt="${u.username}">
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <!-- USERNAME -->
                            <td class="username-cell">${u.username}</td>

                            <!-- EMAIL -->
                            <td class="email-cell">
                                <c:choose>
                                    <c:when test="${not empty u.email}">
                                        ${u.email}
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: #999;">Chưa có email</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <!-- STATUS -->
                            <td class="status-cell">
                                <span class="status-text ${u.status == 1 ? 'status-active' : 'status-locked'}">
                                    <c:choose>
                                        <c:when test="${u.status == 1}">Hoạt động</c:when>
                                        <c:otherwise>Tạm khóa</c:otherwise>
                                    </c:choose>
                                </span>
                            </td>

                            <!-- ACTION ICONS -->
                            <td class="action-cell">
                                <a href="${pageContext.request.contextPath}/admin/customers/detail?id=${u.id}"
                                   class="action-icon-link" title="Xem chi tiết">
                                    <i class="fa-solid fa-pen edit-icon"></i>
                                </a>
                                <c:choose>
                                    <c:when test="${u.status == 1}">
                                        <i class="fa-solid fa-lock toggle-status-icon lock-icon"
                                           data-id="${u.id}" data-status="0"
                                           title="Khóa tài khoản"></i>
                                    </c:when>
                                    <c:otherwise>
                                        <i class="fa-solid fa-unlock toggle-status-icon unlock-icon"
                                           data-id="${u.id}" data-status="1"
                                           title="Mở khóa tài khoản"></i>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
        <!-- PHÂN TRANG -->
        <div class="footer-user" id="footer-pagination">
            <div class="pagination-info">
                <p>
                    Hiển thị
                    <strong>${empty users ? 0 : users.size()}</strong>
                    trên tổng số
                    <strong>${totalUsers}</strong>
                    khách hàng
                    <c:if test="${totalPage > 0}">
                        (Trang ${page}/${totalPage})
                    </c:if>
                </p>
            </div>

            <c:if test="${totalPage > 1}">
                <div class="pagination">
                    <!-- First Page -->
                    <c:if test="${page > 1}">
                        <a href="${pageContext.request.contextPath}/admin/users?page=1&search=${currentSearch}&status=${currentStatus}"
                           class="page-link"
                           title="Trang đầu">
                            «
                        </a>
                    </c:if>

                    <!-- Previous Page -->
                    <c:if test="${page > 1}">
                        <a href="${pageContext.request.contextPath}/admin/users?page=${page-1}&search=${currentSearch}&status=${currentStatus}"
                           class="page-link"
                           title="Trang trước">
                            ‹
                        </a>
                    </c:if>

                    <!-- Page Numbers -->
                    <c:choose>
                        <c:when test="${totalPage <= 7}">
                            <c:forEach begin="1" end="${totalPage}" var="i">
                                <c:choose>
                                    <c:when test="${i == page}">
                                        <span class="page-link active">${i}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/admin/users?page=${i}&search=${currentSearch}&status=${currentStatus}"
                                           class="page-link">
                                                ${i}
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </c:when>

                        <c:otherwise>
                            <c:choose>
                                <c:when test="${page <= 4}">
                                    <c:forEach begin="1" end="5" var="i">
                                        <c:choose>
                                            <c:when test="${i == page}">
                                                <span class="page-link active">${i}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/admin/users?page=${i}&search=${currentSearch}&status=${currentStatus}"
                                                   class="page-link">${i}</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                    <span class="page-link dots">...</span>
                                    <a href="${pageContext.request.contextPath}/admin/users?page=${totalPage}&search=${currentSearch}&status=${currentStatus}"
                                       class="page-link">${totalPage}</a>
                                </c:when>

                                <c:when test="${page >= totalPage - 3}">
                                    <a href="${pageContext.request.contextPath}/admin/users?page=1&search=${currentSearch}&status=${currentStatus}"
                                       class="page-link">1</a>
                                    <span class="page-link dots">...</span>
                                    <c:forEach begin="${totalPage - 4}" end="${totalPage}" var="i">
                                        <c:choose>
                                            <c:when test="${i == page}">
                                                <span class="page-link active">${i}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/admin/users?page=${i}&search=${currentSearch}&status=${currentStatus}"
                                                   class="page-link">${i}</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </c:when>

                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/admin/users?page=1&search=${currentSearch}&status=${currentStatus}"
                                       class="page-link">1</a>
                                    <span class="page-link dots">...</span>

                                    <c:forEach begin="${page - 1}" end="${page + 1}" var="i">
                                        <c:choose>
                                            <c:when test="${i == page}">
                                                <span class="page-link active">${i}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/admin/users?page=${i}&search=${currentSearch}&status=${currentStatus}"
                                                   class="page-link">${i}</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>

                                    <span class="page-link dots">...</span>
                                    <a href="${pageContext.request.contextPath}/admin/users?page=${totalPage}&search=${currentSearch}&status=${currentStatus}"
                                       class="page-link">${totalPage}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>

                    <!-- Next Page -->
                    <c:if test="${page < totalPage}">
                        <a href="${pageContext.request.contextPath}/admin/users?page=${page+1}&search=${currentSearch}&status=${currentStatus}"
                           class="page-link"
                           title="Trang sau">
                            ›
                        </a>
                    </c:if>

                    <!-- Last Page -->
                    <c:if test="${page < totalPage}">
                        <a href="${pageContext.request.contextPath}/admin/users?page=${totalPage}&search=${currentSearch}&status=${currentStatus}"
                           class="page-link"
                           title="Trang cuối">
                            »
                        </a>
                    </c:if>
                </div>
            </c:if>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/asset/js/userManagement.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/dashboardAdmin.js"></script>
</body>
</html>