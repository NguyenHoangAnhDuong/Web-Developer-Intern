<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Giỏ Hàng</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/cart.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
</head>
<body>
<%@ include file="/views/includes/toast.jsp" %>
<jsp:include page="/views/includes/header.jsp" />
<main class="cart-wrap">
    <div class="container">
        <h2 class="cart-title">Giỏ Hàng Của Bạn</h2>
        <div class="cart-actions">
            <button id="clearSelectedBtn" class="clear-selected-btn" type="button" disabled>Xóa tất cả</button>
        </div>

        <div class="table-cart">
            <table>
                <thead>
                <tr>
                    <th><input type="checkbox" id="selectAll" ></th>
                    <th>Sản Phẩm</th>
                    <th>Số Lượng</th>
                    <th>Thành Tiền</th>
                    <th> </th>
                </tr>
                </thead>
                <tbody id="cart-body">

                <c:choose>
                    <c:when test="${not empty cartItems}">
                        <c:forEach var="item" items="${cartItems}">
                            <tr>
                                <td><input type="checkbox" class="select-item"
                                           data-id="${item.vc_id}" data-price="${item.subtotal}" data-unit-price="${item.unit_price}" ${item.stock == 0 ? 'disabled' : ''}></td>

                                    <td class="product-info">
                                        <img src="${item.product_img}" alt="${item.product_name}">
                                        <div class="product-detail">
                                            <span class="product-name">${item.product_name}</span>
                                                <%-- Hiển thị phiên bản và màu sắc --%>
                                            <c:if test="${item.stock == 0}">
                                                <span class="out-of-stock">Hết hàng</span>
                                            </c:if>
                                            <div class="variant-box">
<%--                                                dung lượng--%>
                                                <select class="variant-select"
                                                        onchange="onVariantLevel1Change(this, ${item.vc_id})">
                                                    <c:forEach var="v" items="${item.variants}">
                                                        <option value="${v.variant_id}"
                                                            ${v.variant_id == item.variant_id ? 'selected' : ''}>
                                                                ${v.variant_name}
                                                        </option>
                                                    </c:forEach>
                                                </select>

<%--                                                màu--%>
                                                <select class="color-select"
                                                        onchange="onColorChange(this, ${item.vc_id})">
                                                    <c:forEach var="c" items="${item.colors}">
                                                        <option value="${c.id}"
                                                            ${c.id == item.vc_id ? 'selected' : ''}>
                                                                ${c.color_name}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                        </div>
                                    </td>

                                <td>
                                    <div class="qty-control">
                                        <button class="minus"  ${item.stock == 0 ? 'disabled' : ''} onclick="updateQty(${item.vc_id}, -1)">-</button>
                                        <span class="quantity">${item.quantity}</span>
                                        <button class="plus" onclick="updateQty(${item.vc_id}, 1)">+</button>
                                    </div>
                                </td>
                                <td class="price">
                                    <fmt:formatNumber value="${item.subtotal}" pattern="#,###" />₫ </td>
                                <td>
                                        <%-- Gửi vc_id (ID biến thể màu) để xóa đúng sản phẩm --%>
                                    <button class="delete" onclick="removeItem(${item.vc_id})">Xóa</button>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="5" style="text-align: center; padding: 50px 0;">
                                <i class="fa-solid fa-cart-shopping" style="font-size: 48px; color: #ccc;"></i>
                                <p>Giỏ hàng của bạn đang trống.</p>
                                <a href="${pageContext.request.contextPath}/listproduct" class="btn-buy">Mua sắm ngay</a>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>

                </tbody>
            </table>
        </div>
    </div>
</main>

<div class="sticky-summary-bottom">
    <div class="summary-box">
        <span class="summary-item">Tổng cộng:
            <span id="sub-total" class="subtotal">0đ</span>
        </span>
        <a href="cart?action=checkout"><button class="checkout-btn" ${empty cartItems ? 'disabled' : ''}>Thanh Toán</button></a>
    </div>
</div>

<script src="${pageContext.request.contextPath}/asset/js/cart.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
</body>
</html>