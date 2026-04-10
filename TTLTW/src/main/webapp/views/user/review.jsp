<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đánh giá sản phẩm</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/review.css">
</head>
<body>
<%@ include file="/views/includes/toast.jsp" %>

<div class="overlay">
    <div class="review-modal">
        <a href="${pageContext.request.contextPath}/product-detail?id=${not empty product ? product.id : param.productId}">
            <button class="close-btn" type="button"><i class="fa-solid fa-xmark"></i></button>
        </a>
        <h2>Đánh giá sản phẩm</h2>
        <%-- Thông tin sản phẩm --%>
        <div class="product-info">
            <img src="${product.mainImage}"
                 alt="${product.name}">
            <h3>${product.name}</h3>
        </div>
        <%-- Khu vực không đủ điều kiện --%>
        <div id="notEligible" class="${canReview ? 'hidden' : ''}">
            <p class="not-eligible-msg">
                <i class="fa-solid fa-circle-exclamation"></i>
                Bạn cần mua và nhận hàng thành công mới có thể đánh giá sản phẩm này.
            </p>
        </div>
        <%-- Form đánh giá --%>
        <form id="reviewForm" class="${canReview ? '' : 'hidden'}" action="${pageContext.request.contextPath}/review" method="post">
            <input type="hidden" name="productId" id="productId" value="${not empty product ? product.id : param.productId}">
            <input type="hidden" name="rating" id="ratingInput" value="">
            <%-- Chọn sao --%>
            <div class="rating" id="starRating">
                <span class="star" data-value="1"><i class="fa-regular fa-star"></i><p>Rất tệ</p></span>
                <span class="star" data-value="2"><i class="fa-regular fa-star"></i><p>Tệ</p></span>
                <span class="star" data-value="3"><i class="fa-regular fa-star"></i><p>Tạm ổn</p></span>
                <span class="star" data-value="4"><i class="fa-regular fa-star"></i><p>Tốt</p></span>
                <span class="star" data-value="5"><i class="fa-regular fa-star"></i><p>Rất tốt</p></span>
            </div>
            <p class="rating-error" id="ratingError">Vui lòng chọn số sao.</p>
            <%-- Nhận xét --%>
            <textarea id="comment" name="comment" placeholder="Mời bạn chia sẻ thêm cảm nhận..." maxlength="1000"></textarea>
            <p class="char-count"><span id="charCount">0</span>/1000 ký tự</p>
            <%-- Submit --%>
            <button class="submit-btn" id="submitBtn" type="submit">
                <span id="btnText">Gửi đánh giá</span>
            </button>
        </form>
        <div class="links">
            <a href="#">Quy định đánh giá</a> |
            <a href="#">Chính sách bảo mật thông tin</a>
        </div>
    </div>
</div>
<script>
    const CONTEXT_PATH = '${pageContext.request.contextPath}';
    const productId    = document.getElementById('productId').value;
</script>
<script src="${pageContext.request.contextPath}/asset/js/review.js"></script>
</body>
</html>
