<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    // Lấy dữ liệu đã format từ servlet
    Map<String, Object> orderData = (Map<String, Object>) request.getAttribute("orderData");
    List<Map<String, Object>> items = (List<Map<String, Object>>) request.getAttribute("items");
    Map<String, String> address = (Map<String, String>) request.getAttribute("address");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.6.0/css/all.min.css">
    <meta charset="UTF-8">
    <title>Chi Tiết Đơn Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/order_detail.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/notification.css">
</head>

<body>
<%@ include file="/views/includes/toast.jsp" %>
<jsp:include page="/views/includes/header.jsp"/>

<main class="order-detail-container">

    <section class="card" aria-labelledby="order-heading">
        <header class="order-header">
            <div>
                <div id="order-heading" class="order-id">Đơn hàng #OD<%= String.format("%06d", (Integer)orderData.get("id")) %></div>
                <div class="meta">Ngày đặt: <strong id="order-date"><%= orderData.get("formattedDate") %></strong> • Người nhận: <span
                        id="order-name"><%= address.get("name") %></span></div>
            </div>
            <div style="text-align:right">
                <div id="paid-badge" class="status-pill" aria-live="polite">
                    <span id="paid-text" class="<%= orderData.get("paymentStatusClass") %>"><%= orderData.get("paymentStatusText") %></span>
                </div>
                <div class="muted" style="font-size:12px;margin-top:6px" id="order-status-small">
                    Trạng thái đơn hàng: <strong id="order-status"><%= orderData.get("statusName") %></strong>
                </div>
            </div>
        </header>

        <div class="items" id="order-items" aria-live="polite">
            <% for (Map<String, Object> item : items) { %>
            <article class="item">
                <div class="thumb" aria-hidden="true">
                    <img src="<%= item.get("imagePath") %>"
                         alt="<%= item.get("productName") %>"
                         style="width:82px;height:82px;border-radius:8px;object-fit:cover"/>
                </div>
                <div class="item-info">
                    <div class="name"><%= item.get("productName") %> <%= item.get("variantName") %></div>
                    <div class="variant">Màu: <%= item.get("colorName") %> • Bảo hành: 12 tháng</div>
                    <div class="small" style="margin-top:8px">Mã SP: <span class="muted">TWX<%= String.format("%04d", (Integer)item.get("variantId")) %>-<%= item.get("colorCode") %></span></div>
                </div>
                <div class="price-qty">
                    <div class="price" id="item<%= item.get("id") %>-price"><%= item.get("formattedPrice") %>₫</div>
                    <div class="qty">x<%= item.get("quantity") %></div>
                </div>
                <% int status = (Integer) orderData.get("status"); if (status == 3) { %>
                <div class="item-action">
                    <a class="btn-review" href="${pageContext.request.contextPath}/review?productId=<%= item.get("productId") %>">Đánh giá</a>
                </div>
                <% } %>
            </article>
            <% } %>
        </div>

        <div class="totals" role="region" aria-label="Tổng tiền">
            <div class="row">
                <div class="small">Tạm tính</div>
                <div id="subtotal"><%= orderData.get("formattedSubtotal") %>₫</div>
            </div>
            <div class="row">
                <div class="small">Phí vận chuyển</div>
                <div id="ship-fee"><%= orderData.get("formattedFeeShipping") %>₫</div>
            </div>
            <div class="row">
                <div class="small">Giảm giá</div>
                <div id="discount"><%= orderData.get("formattedDiscountAmount") %>₫</div>
            </div>
            <div class="row total-amount">
                <div>Tổng cộng</div>
                <div id="total"><%= orderData.get("formattedTotalAmount") %>₫</div>
            </div>

            <div class="actions" role="group" aria-label="Hành động đơn hàng">
                <% int status = (Integer) orderData.get("status");
                    int orderId = (Integer) orderData.get("id");
                %>
                <% if (status == 1) { %>
                <button id="btn-request-cancel" class="btn-ghost" data-order-id="<%= orderId %>" onclick="cancelOrder(this.getAttribute('data-order-id'))">Yêu cầu hủy</button>
                <% } else if (status == 3 || status == 4) { %>
                <button class="btn-ghost" data-order-id="<%= orderId %>" onclick="repurchaseOrder(this.getAttribute('data-order-id'))">Mua lại</button>
                <% } %>
            </div>

            <footer class="note">Ghi chú: Bạn có thể yêu cầu hủy nếu đơn hàng chưa giao.</footer>

        </div>
    </section>

    <aside class="right">
        <div class="card section" aria-labelledby="summary-heading">
            <h3 id="summary-heading" style="margin:0 0 8px 0">Tóm tắt</h3>
            <div class="small">Phương thức thanh toán</div>
            <div style="display:flex;justify-content:space-between;align-items:center;margin-top:8px">
                <div>
                    <div id="payment-method"><%= orderData.get("paymentMethodName") %></div>
                </div>
            </div>
            <div class="shipping-detail" style="margin-top:18px">
                <div class="small">Đơn vị vận chuyển</div>
                <div id="shipping-partner"><%= orderData.get("shippingPartner") %></div>
                <div class="small" style="margin-top:12px">Thời gian dự kiến giao</div>
                <div id="expected-delivery"><%= orderData.get("expectedDelivery") %></div>
            </div>
        </div>

        <div class="card section" aria-labelledby="address-heading">
            <h4 id="address-heading" style="margin:0 0 8px 0">Giao tới</h4>
            <div class="address">
                <div id="rec-name"><strong><%= address.get("address") %></strong> • <span class="muted"
                                                                                          id="rec-phone"><%= address.get("phoneNumber") %></span></div>
            </div>
        </div>
    </aside>
</main>

<script src="${pageContext.request.contextPath}/asset/js/orderDetail.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/notification.js"></script>

<script>
    function cancelOrder(orderId) {
        showConfirmDialog('Bạn có chắc chắn muốn hủy đơn hàng này?', 'Xác nhận hủy đơn', {
            iconType: 'warning',
            confirmText: 'Hủy',
            cancelText: 'Không',
            confirmButtonClass: 'btn-confirm-delete'
        }).then(confirmed => {
            if (!confirmed) return;

            fetch('${pageContext.request.contextPath}/api/order/cancel', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'action=cancel&orderId=' + orderId
            })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        setTimeout(() => {
                            showToast('Hủy đơn hàng thành công!', 'success', 3000);
                            setTimeout(() => location.reload(), 1500);
                        }, 300);
                    } else {
                        setTimeout(() => {
                            showToast('Lỗi: ' + data.message, 'error', 3000);
                        }, 300);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    setTimeout(() => {
                        showToast('Có lỗi xảy ra khi hủy đơn hàng', 'error', 3000);
                    }, 300);
                });
        });
    }

    function repurchaseOrder(orderId) {
        showConfirmDialog('Bạn có muốn mua lại đơn hàng này?', 'Xác nhận mua lại', {
            iconType: 'info',
            confirmText: 'Mua lại',
            cancelText: 'Không',
            confirmButtonClass: 'btn-confirm'
        }).then(confirmed => {
            if (!confirmed) return;

            fetch('${pageContext.request.contextPath}/user/order', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'action=repurchase&orderId=' + orderId
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        setTimeout(() => {
                            showToast('Sản phẩm đã được thêm vào giỏ hàng!', 'success', 3000);
                            if (data.redirectUrl) {
                                setTimeout(() => location.href = data.redirectUrl, 1500);
                            } else {
                                setTimeout(() => location.href = '${pageContext.request.contextPath}/user/cart', 1500);
                            }
                        }, 300);
                    } else {
                        setTimeout(() => {
                            showToast('Lỗi: ' + data.message, 'error', 3000);
                        }, 300);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    setTimeout(() => {
                        showToast('Có lỗi xảy ra khi mua lại đơn hàng', 'error', 3000);
                    }, 300);
                });
        });
    }
</script>

</body>
</html>