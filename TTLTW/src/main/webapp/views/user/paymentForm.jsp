<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

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

<!-- ================= HEADER ================= -->
<jsp:include page="../includes/header.jsp"/>

<!-- ================= MAIN ================= -->
<main>

    <!-- ĐỊA CHỈ -->
    <section class="address">
        <i class="fa-solid fa-location-dot"></i>
        <span class="title">Địa Chỉ Nhận Hàng</span>

        <p>
            <strong>${requestScope.address.receiverName}</strong>
            <span>${requestScope.address.phone}</span>
        </p>

        <p>
            ${requestScope.address.fullAddress}
            <span class="default">Mặc định</span>
            <a href="#">Thay đổi</a>
        </p>
    </section>

    <!-- SẢN PHẨM -->
    <section class="products">
        <h3 class="title">Sản phẩm</h3>

        <table class="product-table">
            <thead>
            <tr>
                <th></th>
                <th>Đơn giá</th>
                <th>Số lượng</th>
                <th>Thành tiền</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${cartItems}" var="item">
                <tr class="product-item">
                    <td class="product-info">
                        <img src="${pageContext.request.contextPath}/${item.image}">
                        <div class="details">
                            <p class="name">${item.name}</p>
                            <p class="type">${item.variant}</p>
                        </div>
                    </td>
                    <td class="price">${item.price}₫</td>
                    <td class="quantity">${item.quantity}</td>
                    <td class="total">${item.total}₫</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </section>

    <!-- PHƯƠNG THỨC THANH TOÁN -->
    <form method="post" action="${pageContext.request.contextPath}/checkout">

        <section class="payment">
            <h3 class="title">Phương thức thanh toán</h3>

            <label>
                <input type="radio" name="paymentMethod" value="COD" checked>
                Thanh toán khi nhận hàng (COD)
            </label><br>

            <label>
                <input type="radio" name="paymentMethod" value="BANK">
                Chuyển khoản ngân hàng
            </label>
        </section>

        <section class="summary">
            <h3 class="title">🧾 Tóm tắt đơn hàng</h3>

            <p>Tạm tính: <strong>${subtotal}₫</strong></p>
            <p>Phí vận chuyển: <strong>${shippingFee}₫</strong></p>
            <p>Giảm giá: <strong>${discount}₫</strong></p>

            <p class="total">
                Tổng cộng: <strong>${grandTotal}₫</strong>
            </p>

            <button type="submit" class="round-black-btn">
                Đặt hàng
            </button>
        </section>

    </form>


    <!-- TỔNG TIỀN -->
    <section class="summary">
        <h3 class="title">🧾 Tóm tắt đơn hàng</h3>
        <p>Tạm tính: <strong>${subtotal}₫</strong></p>
        <p>Phí vận chuyển: <strong>${shippingFee}₫</strong></p>
        <p>Giảm giá: <strong>${discount}₫</strong></p>
        <p class="total">Tổng cộng: <strong>${grandTotal}₫</strong></p>

        <button type="submit" class="round-black-btn" style="width:150px">
            Đặt hàng
        </button>
        </form>
    </section>

</main>

<script src="${pageContext.request.contextPath}/asset/js/checkout.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
</body>
</html>
