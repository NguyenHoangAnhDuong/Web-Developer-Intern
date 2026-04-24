<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="context-path" content="${pageContext.request.contextPath}">
    <title>Quản lý đánh giá</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/feedbackManagement.css">
</head>
<body class="admin-body">

<jsp:include page="/views/includes/sideBarAdmin.jsp"/>

<div class="admin-main">
    <div class="admin-topbar">
        <h1>Quản lý đánh giá</h1>
        <span class="total-label">Tổng: <strong>${total}</strong> đánh giá</span>
    </div>
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-label">Tổng đánh giá</div>
            <div class="stat-value">${total}</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Đang hiển thị</div>
            <div class="stat-value green">${showing}</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Chờ duyệt</div>
            <div class="stat-value amber">${pending}</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Đã ẩn</div>
            <div class="stat-value red">${hidden}</div>
        </div>
    </div>
    <form method="get" action="${pageContext.request.contextPath}/admin/feedbacks" class="toolbar">
        <input type="hidden" name="action" value="list"/>
        <input type="text" name="keyword" placeholder="Tìm theo tên, sản phẩm, nội dung..."
               value="${keyword}" class="toolbar-search"/>
        <select name="star" class="toolbar-select">
            <option value="">Tất cả số sao</option>
            <c:forEach var="s" items="${stars}">
                <option value="${s}" ${starParam == s ? 'selected' : ''}>
                        ${s} sao
                </option>
            </c:forEach>
        </select>
        <select name="status" class="toolbar-select">
            <option value="">Tất cả trạng thái</option>
            <option value="show"    ${status == 'show'    ? 'selected' : ''}>Đang hiển thị</option>
            <option value="pending" ${status == 'pending' ? 'selected' : ''}>Chờ duyệt</option>
            <option value="hide"    ${status == 'hide'    ? 'selected' : ''}>Đã ẩn</option>
        </select>
        <button type="submit" class="btn-search">
            <i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm
        </button>
        <a href="${pageContext.request.contextPath}/admin/feedbacks" class="btn-reset">
            <i class="fa-solid fa-rotate-left"></i> Hoàn tác
        </a>
    </form>
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th style="width:40px">#</th>
                <th style="width:140px">Người dùng</th>
                <th style="width:180px">Sản phẩm</th>
                <th style="width:80px">Số sao</th>
                <th>Nội dung</th>
                <th style="width:120px">Ngày đăng</th>
                <th style="width:100px">Trạng thái</th>
                <th style="width:160px">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty feedbacks}">
                    <tr>
                        <td colspan="8" class="no-data">
                            <i class="fa-regular fa-comment-dots"></i>
                            Không có đánh giá nào
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${feedbacks}" var="fb" varStatus="st">
                        <tr>
                            <td class="td-center">${st.index + 1}</td>
                            <td>
                                <div class="user-cell">
                                    <div class="avatar">${fb.userInitial}</div>
                                    <span>${fb.username != null ? fb.username : 'User #'.concat(fb.userId)}</span>
                                </div>
                            </td>
                            <td class="td-ellipsis" title="${fb.productName}">${fb.productName}</td>
                            <td>
                                <span class="stars stars-${fb.rating}">
                                    <c:forEach begin="1" end="5" var="i">
                                        <c:choose>
                                            <c:when test="${i <= fb.rating}">★</c:when>
                                            <c:otherwise>☆</c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </span>
                            </td>
                            <td class="td-ellipsis" title="${fb.comment}">${fb.comment}</td>
                            <td class="td-date">
                                    ${fb.createdAtFormatted}
                            </td>
                            <td>
                                 <span class="badge badge-${fb.statusLabel}">
                                      <c:choose>
                                          <c:when test="${fb.status == 1}">Hiển thị</c:when>
                                          <c:when test="${fb.status == 0}">Đã ẩn</c:when>
                                          <c:otherwise>Chờ duyệt</c:otherwise>
                                      </c:choose>
                                 </span>
                            </td>
                            <td>
                                <div class="action-group">
                                    <button class="btn-view"
                                            data-username="${fb.username}"
                                            data-product="${fb.productName}"
                                            data-rating="${fb.rating}"
                                            data-comment="${fn:escapeXml(fb.comment)}"
                                            data-date="${fb.createdAt}"
                                            data-status="${fb.status}"
                                            data-id="${fb.id}">
                                        Xem
                                    </button>
                                    <c:if test="${fb.status != 1}">
                                        <a href="${pageContext.request.contextPath}/admin/feedbacks?action=approve&id=${fb.id}&keyword=${keyword}&star=${starParam}&status=${status}"
                                           class="btn-action btn-approve">
                                            Duyệt
                                        </a>
                                    </c:if>
                                    <c:if test="${fb.status != 0}">
                                        <a href="${pageContext.request.contextPath}/admin/feedbacks?action=hide&id=${fb.id}&keyword=${keyword}&star=${starParam}&status=${status}"
                                           class="btn-action btn-hide">
                                            Ẩn
                                        </a>
                                    </c:if>
                                    <a href="${pageContext.request.contextPath}/admin/feedbacks?action=delete&id=${fb.id}&keyword=${keyword}&star=${starParam}&status=${status}"
                                       class="btn-action btn-delete">
                                        Xóa
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
    <c:if test="${totalPages > 1}">
        <div class="pagination">
        <span class="pg-info">
            Hiển thị ${(currentPage - 1) * 10 + 1}–${currentPage * 10 > totalItems ? totalItems : currentPage * 10}
            / ${totalItems} đánh giá
        </span>
            <div class="pg-buttons">
                <c:choose>
                    <c:when test="${currentPage > 1}">
                        <a href="?action=list&page=${currentPage - 1}&keyword=${keyword}&star=${starParam}&status=${status}"
                           class="pg-btn">
                            <i class="fa-solid fa-chevron-left"></i>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <button class="pg-btn" disabled>
                            <i class="fa-solid fa-chevron-left"></i>
                        </button>
                    </c:otherwise>
                </c:choose>
                <c:set var="startPage" value="${currentPage > 3 ? currentPage - 2 : 1}"/>
                <c:set var="endPage"   value="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}"/>
                <c:if test="${startPage > 1}">
                    <a href="?action=list&page=1&keyword=${keyword}&star=${starParam}&status=${status}"
                       class="pg-btn">1</a>
                    <c:if test="${startPage > 2}">
                        <span class="pg-dots">…</span>
                    </c:if>
                </c:if>
                <c:forEach var="i" begin="${startPage}" end="${endPage}">
                    <c:choose>
                        <c:when test="${i == currentPage}">
                            <span class="pg-btn active">${i}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="?action=list&page=${i}&keyword=${keyword}&star=${starParam}&status=${status}"
                               class="pg-btn">${i}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                <c:if test="${endPage < totalPages}">
                    <c:if test="${endPage < totalPages - 1}">
                        <span class="pg-dots">…</span>
                    </c:if>
                    <a href="?action=list&page=${totalPages}&keyword=${keyword}&star=${starParam}&status=${status}"
                       class="pg-btn">${totalPages}</a>
                </c:if>
                <c:choose>
                    <c:when test="${currentPage < totalPages}">
                        <a href="?action=list&page=${currentPage + 1}&keyword=${keyword}&star=${starParam}&status=${status}"
                           class="pg-btn">
                            <i class="fa-solid fa-chevron-right"></i>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <button class="pg-btn" disabled>
                            <i class="fa-solid fa-chevron-right"></i>
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:if>
</div>
<div class="modal-overlay" id="modal-overlay" onclick="closeDetail()">
    <div class="modal-box" onclick="event.stopPropagation()">
        <div class="modal-header">
            <h2>Chi tiết đánh giá</h2>
            <button class="modal-close" onclick="closeDetail()">
                <i class="fa-solid fa-xmark"></i>
            </button>
        </div>
        <div class="modal-body">
            <div class="detail-row">
                <span class="detail-label">Người dùng</span>
                <span class="detail-val" id="d-user"></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Sản phẩm</span>
                <span class="detail-val" id="d-product"></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Số sao</span>
                <span class="detail-val" id="d-star"></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Nội dung</span>
                <span class="detail-val" id="d-comment" style="white-space:pre-wrap;line-height:1.7"></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Ngày đăng</span>
                <span class="detail-val" id="d-date"></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Trạng thái</span>
                <span class="detail-val" id="d-status"></span>
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn-action btn-view" onclick="closeDetail()">Đóng</button>
            <a id="d-approve-link" class="btn-action btn-approve">Duyệt</a>
            <a id="d-hide-link" class="btn-action btn-hide">Ẩn</a>
            <a id="d-delete-link" class="btn-action btn-delete">Xóa</a>
        </div>
    </div>
</div>
<script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/dashboardAdmin.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/feedbackManagement.js"></script>
</body>
</html>
