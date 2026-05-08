<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN"/>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="isAjax" value="${param.ajax == 'true'}"/>

<c:if test="${!isAjax}">
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản Lý Đơn Hàng</title>

        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/admin-orders.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">
    </head>
    <body>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>

    <div class="app">
    <%@ include file="/views/includes/sideBarAdmin.jsp" %>

    <main class="content">
    <div class="orders-section">

    <h2>Quản Lý Đơn Hàng</h2>

    <div class="search-filter-bar">
        <div class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="searchInput"
                   placeholder="Tìm theo mã đơn hoặc tên khách hàng">
        </div>

        <div class="filter-options">
            <select id="statusFilter" class="filter-select">
                <option value="">Tất cả trạng thái</option>
                <option value="1">Đang lên đơn</option>
                <option value="2">Đang giao</option>
                <option value="3">Đã giao</option>
                <option value="5">Hủy</option>
            </select>
        </div>
    </div>


    <table class="orders-table" id="ordersTable">
    <thead>
    <tr>
        <th>Mã ĐH</th>
        <th>Khách hàng</th>
        <th>Số điện thoại</th>
        <th>Ngày đặt</th>
        <th>Trạng thái</th>
        <th>Thanh toán</th>
        <th>Tổng</th>
    </tr>
    </thead>
    <tbody>
</c:if>

<%--AJAX--%>
<c:choose>
    <c:when test="${not empty orders}">
        <c:forEach var="item" items="${orders}">
            <tr class="order-row" data-order-id="${item.order.id}" style="cursor: pointer;">
                <td>DH${item.order.id}</td>
                <!-- Lấy tên khách từ Map -->
                <td>${item.customerName}</td>
                <td>${item.customerPhone}</td>
                <td>${item.order.createdAt}</td>
                <td>
                    <select class="status-select status-${item.order.status}" data-id="${item.order.id}" onclick="event.stopPropagation();">
                        <option value="1" ${item.order.status == 1 ? 'selected' : ''}>Đang lên đơn</option>
                        <option value="2" ${item.order.status == 2 ? 'selected' : ''}>Đang giao</option>
                        <option value="3" ${item.order.status == 3 ? 'selected' : ''}>Đã giao</option>
                        <option value="4" ${item.order.status == 4 ? 'selected' : ''}>Đã hủy</option>
                    </select>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${item.order.paymentTypeId == 1}">
                            COD
                        </c:when>
                        <c:otherwise>
                            CK
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <fmt:formatNumber value="${item.order.totalAmount}" type="number" pattern="#,###" groupingUsed="true"/>₫
                </td>
            </tr>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <tr>
            <td colspan="7" style="text-align:center;">Không có đơn hàng nào</td>
        </tr>
    </c:otherwise>
</c:choose>

<c:if test="${!isAjax}">
    </tbody>
    </table>

    </div>
    </main>
    </div>

    <div id="orderDetailModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Chi Tiết Đơn Hàng #<span id="modalOrderId"></span></h2>
                <button class="modal-close" onclick="closeOrderDetailModal()">&times;</button>
            </div>
            <div class="modal-body">
                <div class="order-detail-section">
                    <h3>Thông Tin Khách Hàng</h3>
                    <div class="detail-row">
                        <span class="detail-label">Tên khách:</span>
                        <span class="detail-value" id="customerNameDetail"></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Số điện thoại:</span>
                        <span class="detail-value" id="customerPhoneDetail"></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Địa chỉ:</span>
                        <span class="detail-value" id="customerAddressDetail"></span>
                    </div>
                </div>
                <div class="order-detail-section">
                    <h3>Thông Tin Đơn Hàng</h3>
                    <div class="detail-row">
                        <span class="detail-label">Ngày đặt:</span>
                        <span class="detail-value" id="orderDateDetail"></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Trạng thái:</span>
                        <span class="detail-value" id="orderStatusDetail"></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Thanh toán:</span>
                        <span class="detail-value" id="paymentMethodDetail"></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Ghi chú:</span>
                        <span class="detail-value" id="orderNoteDetail"></span>
                    </div>
                    <div class="detail-row" id="cancellationReasonRow" style="display:none;">
                        <span class="detail-label">Lý do hủy:</span>
                        <span class="detail-value" id="cancellationReasonDetail"></span>
                    </div>
                </div>
                <div class="order-detail-section">
                    <h3>Sản Phẩm</h3>
                    <div class="order-items" id="orderItemsDetail"></div>
                </div>
                <div class="order-detail-section">
                    <h3>Tóm Tắt Thanh Toán</h3>
                    <div class="detail-row">
                        <span class="detail-label">Tiền hàng:</span>
                        <span class="detail-value" id="subtotalDetail"></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Chiết khấu:</span>
                        <span class="detail-value" id="discountDetail"></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Phí vận chuyển:</span>
                        <span class="detail-value" id="shippingFeeDetail"></span>
                    </div>
                    <div class="detail-row" style="border-top: 2px solid #f0f0f0; padding-top: 10px; margin-top: 10px;">
                        <span class="detail-label" style="font-size: 16px; font-weight: 700;">Tổng cộng:</span>
                        <span class="detail-value highlight" style="font-size: 16px;" id="totalDetail"></span>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn-cancel" onclick="closeOrderDetailModal()">Đóng</button>
                <button class="btn-cancel-order" id="cancelOrderBtn" onclick="openCancelReasonModal()" style="display:none;">Hủy Đơn Hàng</button>
            </div>
        </div>
    </div>
    <div id="cancelReasonModal" class="modal cancel-reason-modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Hủy Đơn Hàng #<span id="cancelOrderId"></span></h2>
                <button class="modal-close" onclick="closeCancelReasonModal()">&times;</button>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label for="cancellationReasonInput">Lý do hủy đơn hàng <span style="color: #dc2626;">*</span></label>
                    <textarea id="cancellationReasonInput" placeholder="Nhập lý do hủy đơn hàng..." maxlength="500"></textarea>
                    <div class="char-count"><span id="charCount">0</span>/500</div>
                </div>
                <p style="color: #666; font-size: 13px; margin-bottom: 0;">Lý do hủy sẽ giúp chúng tôi cải thiện dịch vụ. Vui lòng cung cấp thông tin chi tiết.</p>
            </div>
            <div class="modal-footer">
                <button class="btn-cancel" onclick="closeCancelReasonModal()">Quay Lại</button>
                <button class="btn-cancel-order" id="confirmCancelBtn" onclick="confirmCancelOrder()">Xác Nhận Hủy</button>
            </div>
        </div>
    </div>
    <div id="toast" class="toast"></div>

    <script src="${pageContext.request.contextPath}/asset/js/orderAdmin.js"></script>
    <script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
    </body>
    </html>
</c:if>
