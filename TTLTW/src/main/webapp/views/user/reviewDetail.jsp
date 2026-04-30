<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${totalFeedbacks} đánh giá - ${product.name}</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/feedbackDetail.css">
</head>
<body>
<jsp:include page="/views/includes/header.jsp"/>
<div class="app-wrapper">
    <div class="container-header">
        <div class="breadcrumb">
            <a href="${pageContext.request.contextPath}/listproduct">Điện thoại</a>
            <span>›</span>
            <a href="${pageContext.request.contextPath}/product-detail?id=${product.id}">${product.name}</a>
            <span>›</span>
            <span>Tất cả đánh giá</span>
        </div>
    </div>
    <section class="review-section">
        <h2>${totalFeedbacks} đánh giá ${product.name}</h2>
        <div class="review-summary">
            <%-- Điểm tổng --%>
            <div class="review-score">
                <div class="score-big">
                    <span class="score">${averageRating}</span>
                    <span class="outof">/5</span>
                </div>
                <div class="stars">
                    <c:forEach begin="1" end="5" var="i">
                        <c:choose>
                            <c:when test="${i <= averageRating}">
                                <i class="fa-solid fa-star"></i>
                            </c:when>
                            <c:when test="${i - 0.5 <= averageRating}">
                                <i class="fa-solid fa-star-half-stroke"></i>
                            </c:when>
                            <c:otherwise>
                                <i class="fa-regular fa-star"></i>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
                <p class="review-count">${totalFeedbacks} đánh giá</p>
                <a href="${pageContext.request.contextPath}/review?productId=${product.id}">
                    <button class="btn btn-primary">Viết đánh giá</button>
                </a>
            </div>
                <%-- Thanh bar — mỗi bar là link lọc --%>
            <div class="review-bars">
                <c:set var="starList" value="5,4,3,2,1"/>
                <c:forEach var="star" items="${starList}">
                    <c:set var="count" value="${starCounts[star]}"/>
                    <c:set var="percentage" value="0"/>
                    <c:if test="${totalFeedbacks > 0}">
                        <c:set var="percentage" value="${count * 100.0 / totalFeedbacks}"/>
                    </c:if>
                    <a href="?productId=${product.id}&star=${star}"
                       class="bar-row ${filterStar == star ? 'active' : ''}">
                        <span class="star-label">
                            ${star} <i class="fa-solid fa-star"></i>
                        </span>
                        <div class="bar">
                            <div class="fill" style="width: ${percentage}%"></div>
                        </div>
                        <span class="bar-pct">
                            <fmt:formatNumber value="${percentage}" pattern="0.#"/>%
                        </span>
                    </a>
                </c:forEach>
            </div>
        </div>
        <%-- Nút lọc theo số sao --%>
        <div class="filter">
            <a href="?productId=${product.id}">
                <button class="${empty filterStar ? 'active' : ''}">Tất cả</button>
            </a>
            <c:forEach var="s" items="5,4,3,2,1">
                <a href="?productId=${product.id}&star=${s}">
                    <button class="${filterStar == s ? 'active' : ''}">
                            ${s} <i class="fa-solid fa-star" style="color:#f5a623;font-size:10px"></i>
                    </button>
                </a>
            </c:forEach>
        </div>
        <%-- Danh sách đánh giá --%>
        <div class="review-list">
            <c:choose>
                <c:when test="${empty feedbacks}">
                    <p class="no-review">Chưa có đánh giá nào cho mức này.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${feedbacks}" var="fb">
                        <div class="review-item">
                            <div class="review-header">
                                <span class="name">
                                    <i class="fa-solid fa-circle-user"></i>
                                    User #${fb.userId}
                                </span>
                                <span class="bought">
                                    <i class="fa-solid fa-circle-check"></i>
                                    Đã mua tại cửa hàng
                                </span>
                            </div>
                            <div class="stars">
                                <c:forEach begin="1" end="5" var="i">
                                    <c:choose>
                                        <c:when test="${i <= fb.rating}">★</c:when>
                                        <c:otherwise>☆</c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </div>
                            <p class="review-text">${fb.comment}</p>
                            <div class="review-footer">
                                <span class="time">
                                    <i class="fa-regular fa-clock"></i>
                                    ${fb.createdAt}
                                </span>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</div>
<jsp:include page="/views/includes/footer.jsp"/>
<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
</body>
</html>
