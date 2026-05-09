<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="isSuper" value="${sessionScope.roleName == 'super_admin'}" />
<c:set var="perms" value="${sessionScope.permissions}" />
<c:set var="canCreateVoucher" value="${isSuper || (perms != null && perms.contains('voucher.create'))}" />
<c:set var="canUpdateVoucher" value="${isSuper || (perms != null && perms.contains('voucher.update'))}" />
<c:set var="canDeleteVoucher" value="${isSuper || (perms != null && perms.contains('voucher.delete'))}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Khuyến mãi - Admin</title>

    <!-- ICON -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css"
          crossorigin="anonymous" referrerpolicy="no-referrer"/>

    <!-- CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/vouchersAdmin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">
</head>
<body>
<%@ include file="/views/includes/toast.jsp" %>
<div class="app">
    <%@ include file="/views/includes/sideBarAdmin.jsp" %>

    <div class="container">
        <!-- Topbar -->
        <div class="topbar">
            <div class="topbar-left">
                <div class="muted hidden">Quản lý mã khuyến mãi</div>
            </div>
        </div>

        <div class="table-container">
            <c:if test="${not empty error}">
                <div class="error-message" style="background-color: #fee; color: #c33; padding: 10px; margin-bottom: 15px; border-radius: 4px; border: 1px solid #fcc;">
                    <i class="fas fa-exclamation-circle"></i> ${error}
                </div>
            </c:if>
            <!-- Toolbar -->
            <div class="toolbar">
                <div class="left">
                    <form method="get" action="${pageContext.request.contextPath}/admin/vouchers"
                          style="display: inline-flex; gap: 10px;">
                        <input type="text" name="keyword" placeholder="Tìm kiếm khuyến mãi..." value="${keyword}">
                        <select name="status" onchange="this.form.submit()">
                            <option value="-1" ${status == -1 ? 'selected' : ''}>Tất cả</option>
                            <option value="1" ${status == 1 ? 'selected' : ''}>Đang áp dụng</option>
                            <option value="0" ${status == 0 ? 'selected' : ''}>Hết hạn</option>
                        </select>
                        <button type="submit" style="display: none;"></button>
                    </form>
                </div>
                <c:if test="${canCreateVoucher}">
                    <button type="button" class="btn-add" id="btnOpenModal">+ Thêm Khuyến mãi</button>
                </c:if>

            </div>

            <!-- Table -->
            <table id="promoTable">
                <thead>
                <tr>
                    <th>Mã KM</th>
                    <th>Loại KM</th>
                    <th>Giảm</th>
                    <th>Giảm tối đa</th>
                    <th>Đơn tối thiểu</th>
                    <th>Số lượng</th>
                    <th>Ngày bắt đầu</th>
                    <th>Ngày kết thúc</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:choose>
                    <c:when test="${not empty vouchers}">
                        <c:forEach var="voucher" items="${vouchers}">

                        <tr>
                                <!-- Mã KM -->
                                <td>${voucher.voucherCode}</td>

                                <!-- Loại KM -->
                                <td>
                                    <c:choose>
                                        <c:when test="${voucher.type == 1}">Phần trăm</c:when>
                                        <c:when test="${voucher.type == 2}">Tiền mặt</c:when>
                                        <c:otherwise>Tặng quà</c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Giảm -->
                                <td>
                                    <c:choose>
                                        <c:when test="${voucher.type == 1}">
                                            ${voucher.discountAmount}%
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${voucher.discountAmount}" pattern="#,###"/> ₫
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Giảm tối đa -->
                                <td>
                                    <fmt:formatNumber value="${voucher.maxReduce}" pattern="#,###"/> ₫
                                </td>

                                <!-- Đơn tối thiểu -->
                                <td>
                                    <fmt:formatNumber value="${voucher.minOrderValue}" pattern="#,###"/> ₫
                                </td>

                                <!-- Số lượng -->
                                <td>${voucher.quantity}</td>

                                <!-- Ngày bắt đầu/kết thúc -->
                            <td data-date="${voucher.startDate != null ? voucher.startDate.toString().substring(0, 10) : ''}">
                                    ${voucher.startDate != null ? voucher.startDate.toString().substring(0, 10) : ''}
                            </td>
                            <td data-date="${voucher.endDate != null ? voucher.endDate.toString().substring(0, 10) : ''}">
                                    ${voucher.endDate != null ? voucher.endDate.toString().substring(0, 10) : ''}
                            </td>
                                <!-- Trạng thái -->
                                <td>
                                    <span class="status ${voucher.status == 1 ? 'active' : 'inactive'}">
                                            ${voucher.status == 1 ? 'Đang áp dụng' : 'Hết hạn'}
                                    </span>
                                </td>

                                <!-- Thao tác -->
                                <td>
                                    <c:if test="${canUpdateVoucher}">
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/admin/vouchers"
                                              style="display:inline;">
                                            <input type="hidden" name="action" value="toggle">
                                            <input type="hidden" name="id" value="${voucher.id}">
                                            <button type="submit" class="btn-toggle" title="${voucher.status == 1 ? 'Tắt' : 'Bật'}">
                                                <i class="fas ${voucher.status == 1 ? 'fa-toggle-on' : 'fa-toggle-off'}"></i>
                                            </button>
                                        </form>

                                        <button
                                                class="btn-edit"
                                                onclick="editRow(this)"
                                                data-id="${voucher.id}"
                                                title="Sửa">
                                            <i class="fas fa-pen-to-square"></i>
                                        </button>
                                    </c:if>

                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <tr>
                            <td colspan="10" style="text-align:center; padding:20px;">
                                Không có dữ liệu khuyến mãi
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
            <!-- Modal Form -->
            <div class="modal" id="promoModal">
                <div class="modal-content">
                    <h3>Thêm Khuyến mãi mới</h3>

                    <form id="promoForm" method="post"
                          action="${pageContext.request.contextPath}/admin/vouchers">

                        <input type="hidden" name="action" id="formAction">
                        <input type="hidden" name="id" id="editId">
                        <label>Mã KM</label>
                        <input type="text" name="voucherCode" id="promoCode" required>

                        <label>Loại khuyến mãi</label>
                        <select name="type" id="promoType" required>
                            <option value="1">Giảm theo %</option>
                            <option value="2">Giảm theo số tiền</option>
                            <option value="3">Tặng quà</option>
                        </select>

                        <label>Mức giảm</label>
                        <input type="number" name="discountAmount" id="discountValue" min="0" required>


                        <label>Giảm tối đa (₫)</label>
                        <input type="number" name="maxReduce" id="maxDiscount" min="0" required>

                        <label>Đơn tối thiểu (₫)</label>
                        <input type="number" name="minOrderValue" id="minOrder" min="0" required>

                        <label>Số lượng</label>
                        <input type="number" name="quantity" id="quantity" min="1" required>

                        <label>Ngày bắt đầu</label>
                        <input type="date" name="startDate" id="startDate" required>

                        <label>Ngày kết thúc</label>
                        <input type="date" name="endDate" id="endDate" required>

                        <div class="modal-buttons">
                            <button type="submit" class="btn-save">Lưu</button>
                            <button type="button" class="btn-cancel" id="btnCloseModal">Hủy</button>

                        </div>
                    </form>

                </div>
            </div>
            <!-- Footer -->
            <div class="footer" id="footer-pagination">
                <p>
                    Hiển thị ${empty vouchers ? 0 : vouchers.size()} mã khuyến mãi
                    (Tổng: ${totalVoucher})
                </p>

                <div class="pagination">
                    <c:if test="${page > 1}">
                        <a href="${pageContext.request.contextPath}/admin/vouchers?page=${page-1}&keyword=${keyword}&status=${status}" class="page-arrow" title="Trang trước">
                            <i class="fas fa-chevron-left"></i></a>
                    </c:if>
                    <c:if test="${page == 1}">
                        <span class="page-arrow disabled"><i class="fas fa-chevron-left"></i></span>
                    </c:if>
                    <c:forEach begin="1" end="${totalPage}" var="i">
                        <c:choose>
                            <c:when test="${i == page}">
                                <span class="active">${i}</span>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/admin/vouchers?page=${i}&keyword=${keyword}&status=${status}"class="page-num">
                                        ${i}
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                    <c:if test="${page < totalPage}">
                        <a href="${pageContext.request.contextPath}/admin/vouchers?page=${page+1}&keyword=${keyword}&status=${status}"class="page-arrow" title="Trang sau">
                            <i class="fas fa-chevron-right"></i>
                        </a>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- JS -->
<script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/voucherAdmin.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/dashboardAdmin.js"></script>
</body>
</html>
