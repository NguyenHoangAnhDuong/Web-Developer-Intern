<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.6.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/checkout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/cart.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
</head>
<body>
<%@ include file="/views/includes/toast.jsp" %>
<jsp:include page="/views/includes/header.jsp" />
<main>
    <section class="address">
        <i class="fa-solid fa-location-dot"></i>
        <span class="title">Địa Chỉ Nhận Hàng</span>
        <c:choose>
            <c:when test="${not empty defaultAddress}">
                <p>
                    <strong>${defaultAddress.name}</strong>
                    <span>(${defaultAddress.phoneNumber})</span>
                </p>
                <p>
                        ${defaultAddress.address}
                    <span class="default">Mặc định</span>
                    <a href="${pageContext.request.contextPath}/user/addresses">Thay đổi</a>
                </p>

                <input type="hidden" name="addressId" value="${defaultAddress.id}">
            </c:when>
            <c:otherwise>
                <p>Chưa có địa chỉ. <a href="${pageContext.request.contextPath}/user/addresses">Thêm địa chỉ mới</a></p>
            </c:otherwise>
        </c:choose>
    </section>
<%--     Danh sách sản phẩm trong đơn hàng--%>
    <section class="products">
        <h3 class="title">Sản phẩm</h3>
        <table class="product-table">
            <thead>
            <tr>
                <th>Sản phẩm</th>
                <th>Đơn giá</th>
                <th>Số lượng</th>
                <th>Thành tiền</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${checkoutItems}">
                <input type="hidden" name="selectedItems" value="${item.vc_id}" form="orderForm">
                <tr class="product-item">
                    <td class="product-info">
                        <img src="${pageContext.request.contextPath}/assert/img/product/${item.product_img}" alt="${item.product_name}"/>
                        <div class="details">
                            <p class="name">${item.product_name}</p>
                            <p class="type">${item.variant_name} | ${item.color_name}</p>
                        </div>
                    </td>
                    <td class="price">
                        <fmt:formatNumber value="${item.unit_price}" pattern="#,###"/>₫
                    </td>
                    <td class="quantity">${item.quantity}</td>
                    <td class="total">
                        <fmt:formatNumber value="${item.subtotal}" pattern="#,###"/>₫
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </section>


<%--      Danh sách voucher có thể áp dụng--%>
    <section class="voucher-section">
        <h3 class="title">Voucher Ưu Đãi</h3>
        <div class="voucher-wrapper">
            <div class="voucher-scroll" id="voucherScroll">
                <div class="voucher-container">
                    <c:forEach var="v" items="${availableVouchers}">
                        <div class="voucher">
                            <div class="voucher-left">
                                <div class="icon"><img src="assert/img/logo.png" alt="logo"></div>
                            </div>
                            <div class="voucher-right">
                                <div>
                                    <h3>Giảm <fmt:formatNumber value="${v.discountAmount}"/>₫</h3>
                                    <p>Đơn từ: <fmt:formatNumber value="${v.minOrderValue}"/>₫</p>
                                    <p>Tối đa: <fmt:formatNumber value="${v.maxReduce}"/>₫</p>
                                </div>
                                <button type="button"
                                        data-code="${v.voucherCode}"
                                        data-discount="${v.discountAmount}"
                                        data-min-order="${v.minOrderValue}"
                                        data-max-reduce="${v.maxReduce}"
                                        data-type="${v.type}"
                                        onclick="applyVoucherFromBtn(this)">
                                    Áp dụng
                                </button>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
    </section>

    <form action="placeOrder" method="POST" id="orderForm">
        <input type="hidden" name="addressId" value="${defaultAddress.id}">
        <c:forEach var="item" items="${checkoutItems}">
            <input type="hidden" name="selectedItems" value="${item.vc_id}">
        </c:forEach>
        <%--     ghi chú cho đơn hàng--%>
        <section class="buyer-note">
            <h3 class="title" >Ghi chú cho đơn hàng</h3>
            <textarea name="buyerNote"
                      class="note-box"
                      placeholder="Ví dụ: giao giờ hành chính, gọi trước khi giao..."></textarea>
        </section>
<%--            chọn đơn vị vận chuyển --%>
            <section class="shipping-method">
                <h3 class="title"><i class="fa-solid fa-truck-fast"></i> Đơn vị vận chuyển</h3>
                <div class="shipping-container">
                    <c:forEach var="option" items="${shippingOptions}" varStatus="loop">
                        <label class="shipping-card">
                            <input type="radio" name="shippingPartner" value="${option.shippingMethod}"
                                   data-fee="${option.baseFee}"
                                ${loop.first ? 'checked' : ''}
                                              class="shipping-option">
                            <div class="shipping-info">
                    <span class="name">
                        <c:choose>
                            <c:when test="${option.shippingMethod == 'standard'}">Giao hàng tiêu chuẩn</c:when>
                            <c:otherwise>Giao hàng hỏa tốc</c:otherwise>
                        </c:choose>
                    </span>
                                <span class="desc">Dự kiến: ${option.estimatedDays} ngày</span>
                            </div>
                            <span class="price"><fmt:formatNumber value="${option.baseFee}" pattern="#,###"/>₫</span>
                        </label>
                    </c:forEach>
                </div>
                <input type="hidden" name="finalTotal" id="finalTotalInput" value="${subtotal + shippingFee}">
                <input type="hidden" name="shippingFee" id="shippingFeeInput" value="${shippingFee}">
            </section>
            <%--      chọn phương thức thanh toán--%>
        <section class="payment">
            <h3 class="title">Phương thức thanh toán</h3>
            <label><input type="radio" name="payment" value="cod" checked> Thanh toán khi nhận hàng (COD)</label><br>
            <label><input type="radio" name="payment" value="bank"> Chuyển khoản ngân hàng</label>
        </section>
<%--      tóm tắt đơn hàng và nút đặt hàng--%>
        <section class="summary"> 
            <h3 class="title">🧾 Tóm tắt đơn hàng</h3>
            <p>Tạm tính: <strong id="subtotal-val" data-value="${subtotal}"><fmt:formatNumber value="${subtotal}" pattern="#,###"/>₫</strong></p>
            <p>Phí vận chuyển: <strong id="shipping-val" data-value="${shippingFee}"><fmt:formatNumber value="${shippingFee}" pattern="#,###" />₫</strong></p>

            <p>Giảm giá: <strong style="color: red;">-<span id="discount-display">0</span>₫</strong></p>
            <input type="hidden" name="fullName" value="${defaultAddress.name}">
            <input type="hidden" name="phone" value="${defaultAddress.phoneNumber}">
            <input type="hidden" name="fullAddress" value="${defaultAddress.address}">
            <p class="total">Tổng cộng: <strong id="final-total-display"><fmt:formatNumber value="${subtotal + shippingFee}" pattern="#,###"/>₫</strong></p>
            <input type="hidden" name="appliedVoucher" id="appliedVoucherInput" value="">
            <input type="hidden" name="addressId" value="${defaultAddress.id}">
            <button type="submit" class="round-black-btn">Đặt hàng</button>
        </section>
    </form>
</main>
<script src="${pageContext.request.contextPath}/asset/js/cart.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/checkout.js"></script>
</body>
</html>