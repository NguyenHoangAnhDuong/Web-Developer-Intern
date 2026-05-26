<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết sản phẩm</title>

    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/productDetail.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/listproduct.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/listVouchers.css">
</head>
<body>
<%@ include file="/views/includes/toast.jsp" %>
<div class="app-wrapper">
    <!-- Header -->
    <jsp:include page="/views/includes/header.jsp"/>
    <!-- End Header -->
    <!--header chi tiết sản phẩm-->
    <div class="container-header">
        <div class="breadcrumb">
            <a href="${pageContext.request.contextPath}/listproduct?categoryId=${product.category.id}">${product.category.name}</a>
            <span>›</span>
            <a href="${pageContext.request.contextPath}/product-detail?id=${product.id}">${product.name}</a>
        </div>
        <h1 class="product-title">
            ${product.name}
            <span class="sold-info">Đã bán ${product.totalSold}</span>
                <span class="rating">
                    <c:choose>
                    <c:when test="${totalFeedbacks > 0}">
                         <i class="fa-solid fa-star" style="color: #f5a623;"></i>
                            ${averageRating}
                        <span style="font-size:12px;color:#888">(${totalFeedbacks} đánh giá)</span>
                    </c:when>
                         <c:otherwise>
                            <i class="fa-regular fa-star" style="color: #ccc;"></i>
                            <span style="font-size:13px;color:#aaa">Chưa có đánh giá</span>
                         </c:otherwise>
                    </c:choose>
</span>
            <a href="#tech-specs" class="spec-link">Thông số</a>
        </h1>
    </div>
    <!--        main nội dung-->
    <div class="box-main">
        <!--            cột trái-->
        <div class="product-info-left">
            <div class="product-detail">
                <div class="product-gallery">
                    <c:set var="imgFolder"
                           value="${product.category.id == 1 ? 'product' : 'accesory'}"/>

                    <div class="main">
                        <c:choose>
                            <c:when test="${not empty images}">
                                <img class="img-feature"
                                     src="${images[0].imgPath}"
                                     alt="${product.name}">
                            </c:when>
                            <c:otherwise>
                                <img class="img-feature"
                                     src="${product.mainImage}"
                                     alt="${product.name}">
                            </c:otherwise>
                        </c:choose>

                    <div class="control prev"><i class="fas fa-angle-left"></i></div>
                        <div class="control next"><i class="fas fa-angle-right"></i></div>
                    </div>

                    <div class="list-image">
                        <c:choose>
                            <c:when test="${not empty images}">
                                <c:set var="imgFolder" value="${product.category.id == 1 ? 'product' : 'accesory'}"/>
                                <c:forEach items="${images}" var="img">
                                    <div><img
                                            src="${img.imgPath}"
                                            alt="${product.name}"></div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <c:set var="imgFolder" value="${product.category.id == 1 ? 'product' : 'accessory'}"/>
                                <div><img
                                        src="${product.mainImage}"
                                        alt="${product.name}"></div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            <!--  Thông số kỹ thuật -->
            <div id="tech-specs" class="tech-specs">
                <h3 class="specs-title">Cấu hình &amp; Bộ nhớ</h3>
                <table class="specs-table">
                    <c:forEach items="${techSpecs}" var="spec">
                        <tr>
                            <td>${spec.name}</td>
                            <td>${spec.value}</td>
                        </tr>
                    </c:forEach>

                </table>
            </div>
            <!--  Đặc điểm nổi bật -->
            <div class="product-highlights">
                <h3>Đặc điểm nổi bật</h3>
                <ul>
                    <c:choose>
                        <c:when test="${not empty product.description}">
                            <c:set var="highlights" value="${fn:split(product.description, '&#10;')}"/>
                            <c:forEach items="${highlights}" var="highlight">
                                <c:if test="${not empty highlight}">
                                    <li>${highlight}</li>
                                </c:if>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <li>Sản phẩm chất lượng cao</li>
                            <li>Thiết kế hiện đại</li>
                            <li>Hiệu năng mạnh mẽ</li>
                            <li>Bảo hành chính hãng</li>
                            <li>Hỗ trợ khách hàng 24/7</li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>
        </div>
        <!--            cột phải-->
        <div class="product-info-right">
            <div class="product-info">
                <div class="info">
                    <!--  Giá & khuyến mãi -->
                    <h2 class="price-label">Giá sản phẩm</h2>

                    <div class="price-box">
                        <div class="price-content">

                            <c:set var="basePrice" value="0"/>

                            <!-- LẤY GIÁ GỐC -->
                            <c:choose>
                                <c:when test="${not empty defaultVariantColor}">
                                    <c:set var="basePrice" value="${defaultVariantColor.price}"/>
                                </c:when>
                                <c:when test="${not empty variants}">
                                    <c:set var="basePrice" value="${variants[0].basePrice}"/>
                                </c:when>
                            </c:choose>

                            <!-- TÍNH GIÁ SAU DISCOUNT -->
                            <c:set var="discountedPrice"
                                   value="${basePrice * (100 - product.discountPercentage) / 100}"/>

                            <!-- GIÁ MỚI -->
                            <span class="current-price">
            <fmt:formatNumber value="${discountedPrice}"
                              type="number"
                              groupingUsed="true"
                              pattern="#,###"/>₫
        </span>

                            <!-- GIÁ CŨ (LUÔN TỒN TẠI DOM) -->
                            <span class="old-price"
                                  style="${product.discountPercentage > 0 ? '' : 'display:none'}">
            <fmt:formatNumber value="${basePrice}"
                              type="number"
                              groupingUsed="true"
                              pattern="#,###"/>₫
        </span>

                            <!-- DISCOUNT (LUÔN TỒN TẠI DOM) -->
                            <span class="discount"
                                  style="${product.discountPercentage > 0 ? '' : 'display:none'}">
            -${product.discountPercentage}%
        </span>

                        </div>
                    </div>

                    <h2>Chọn phiên bản</h2>
                    <div class="version-select">
                        <c:forEach items="${variants}" var="v" varStatus="st">
                            <button class="version ${st.first ? 'active' : ''}" data-variant-id="${v.id}">
                                    ${v.name}
                            </button>
                        </c:forEach>
                    </div>

                    <h2>Chọn màu sắc</h2>
                    <div class="color-options">
                        <c:forEach items="${colors}" var="c" varStatus="st">
                            <div class="color-item ${st.first ? 'active' : ''}"
                                 data-color-id="${c.color.id}"
                                 data-color-name="${c.color.name}"
                                 data-variant-color-id="${c.id}"
                                 data-price="${c.price}">
        <span class="color-list"
              style="background:${c.color.colorCode}"></span>
                                <span>${c.color.name}</span>
                            </div>
                        </c:forEach>
                    </div>

                </div>
                <div class="note-promotion">
                    <ul>
                        <li>Không áp dụng chung với khuyến mãi khác.</li>
                        <li>Khuyến mãi chưa bao gồm phí giao/chuyển hàng.</li>
                    </ul>
                </div>

                <!--  Thông tin vận chuyển -->
                <div class="shipping-box">
                    <h3>Vận chuyển & Phí ship</h3>
                    <p><i class="fa fa-truck"></i> Giao hàng tận nơi toàn quốc</p>
                    <p>
                        <i class="fa fa-box"></i> Miễn phí vận chuyển nội thành Hà
                        Nội & TP.HCM
                    </p>
                    <p>
                        <i class="fa fa-clock"></i> Thời gian giao hàng: 1 - 3 ngày
                    </p>

                    <!--  Giao đến -->
                    <div class="shipping-destination">
                        <c:choose>
                            <c:when test="${not empty userAddress}">
                                 <span><i class="fa fa-map-marker-alt"></i> Giao đến:<strong>${userAddress.address}</strong>
                                 </span>
                            </c:when>
                            <c:when test="${not empty sessionScope.user}">
                                <span><i class="fa fa-map-marker-alt"></i> Giao đến:<strong>
                                    <a href="${pageContext.request.contextPath}/user/addresses">
                                        Thêm địa chỉ giao hàng
                                    </a></strong>
                                 </span>
                            </c:when>
                            <%-- Chưa đăng nhập --%>
                            <c:otherwise>
                                <span><i class="fa fa-map-marker-alt"></i> Giao đến:<strong>
                                    <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>để xem địa chỉ giao <hàng></hàng></strong>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <!--  Nút hành động -->
                <div class="action-buttons">
                    <button class="btn btn-primary btn-buy">Mua ngay</button>
                    <button class="btn btn-secondary btn-cart"><i class="fa-solid fa-cart-plus" style="margin-right: 5px;"></i>Thêm vào giỏ hàng</button>
                </div>
            </div>

        </div>
    </div>
    <!--  Đánh giá sản phẩm -->
    <section id="review-section" class="review-section">
        <h2>Đánh giá ${product.name}</h2>

        <div class="review-summary">
            <div class="review-score">
                <div class="score-big">
                    <span class="score">${averageRating}</span>
                    <span class="outof">/5</span>
                </div>
                <!-- Hiển thị sao theo averageRating -->
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
            </div>

            <div class="review-bars">
                <c:set var="starList" value="5,4,3,2,1"/>
                <c:forEach var="star" items="${starList}">
                    <c:set var="countStar" value="0"/>
                    <c:forEach items="${feedbacks}" var="fb">
                        <c:if test="${fb.rating == star}">
                            <c:set var="countStar" value="${countStar + 1}"/>
                        </c:if>
                    </c:forEach>
                    <c:set var="percentage" value="0"/>
                    <c:if test="${totalFeedbacks > 0}">
                        <c:set var="percentage" value="${countStar * 100 / totalFeedbacks}"/>
                    </c:if>
                    <div class="bar-row">
                        <span class="star-label">
                            ${star} <i class="fa-solid fa-star" style="color:#f5a623;font-size:12px"></i>
                        </span>
                        <div class="bar">
                            <div class="fill" style="width: ${percentage}%"></div>
                        </div>
                        <span class="bar-pct">
                            <fmt:formatNumber value="${percentage}" pattern="0.#"/>%
                        </span>
                    </div>
                </c:forEach>
            </div>
        </div>

        <div class="review-list">

            <c:if test="${empty feedbacks}">
                <p class="no-review" style="color: red; font-weight: bold;">
                    Chưa có đánh giá nào cho sản phẩm này.
                </p>

            </c:if>

            <c:forEach items="${feedbacks}" var="fb">
                <div class="review-item">

                    <div class="review-header">
        <span class="name">
        User #${fb.userId}
        </span>
                        <span class="bought">Đã mua tại cửa hàng</span>
                    </div>

                    <!-- Hiển thị sao -->
                    <div class="stars">
                        <c:forEach begin="1" end="5" var="i">
                            <c:choose>
                                <c:when test="${i <= fb.rating}">★</c:when>
                                <c:otherwise>☆</c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </div>

                    <p class="review-text">
                            ${fb.comment}
                    </p>

                    <div class="review-footer">
        <span class="time">
                ${fb.createdAt}
        </span>
                    </div>

                </div>
            </c:forEach>

        </div>


        <div class="review-buttons">

            <c:if test="${totalFeedbacks > 0}">
                <a href="${pageContext.request.contextPath}/feedback?productId=${product.id}">
                    <button class="btn-view">Xem ${totalFeedbacks} đánh giá</button>
                </a>
            </c:if>
            <a href="${pageContext.request.contextPath}/review?productId=${product.id}" class="btn btn-primary btn-write">
                Viết đánh giá
            </a>
        </div>
    </section>
    <!-- Sản phẩm liên quan -->
    <section class="product-related">
        <h2>Sản phẩm liên quan</h2>
        <div id="product-list" class="product-list">
            <c:forEach items="${relatedProducts}" var="product">
                <div class="product-card">
                    <a href="${pageContext.request.contextPath}/product-detail?id=${product.id}">
                        <div class="product-img">
                            <c:set var="relatedImgPath" value="${product.categoryId == 1 ? 'product' : 'accesory'}"/>
                            <img src="${product.image}"
                                 alt="${product.name}">
                            <c:if test="${product.discount > 0}">
                                <span class="discount-badge">-${product.discount}%</span>
                            </c:if>
                        </div>
                    </a>
                    <div class="product-info">
                        <h2>${product.name}</h2>
                        <div class="price-wrap">
            <span class="price-new">
              <fmt:formatNumber value="${product.priceNew}" type="number" groupingUsed="true" pattern="#,###"/>₫
            </span>
                            <c:if test="${product.priceOld > product.priceNew}">
              <span class="price-old">
                <fmt:formatNumber value="${product.priceOld}" type="number" groupingUsed="true" pattern="#,###"/>₫
              </span>
                            </c:if>
                        </div>
                        <c:if test="${not empty product.variants}">
                            <c:set var="hasValidVariant" value="false"/>
                            <c:forEach var="variant" items="${product.variants}">
                                <c:if test="${not empty variant.name and not hasValidVariant}">
                                    <c:set var="hasValidVariant" value="true"/>
                                </c:if>
                            </c:forEach>
                            <c:if test="${hasValidVariant}">
                                <div class="capacity">
                                    <c:set var="firstActive" value="true"/>
                                    <c:forEach var="variant" items="${product.variants}" varStatus="status">
                                        <c:if test="${not empty variant.name}">
                                            <button class="${firstActive ? 'active' : ''}"
                                                    data-id="${variant.id}"
                                                    data-price="${variant.priceNew}"
                                                    data-old-price="${variant.priceOld}">
                                                    ${variant.name}
                                            </button>
                                            <c:set var="firstActive" value="false"/>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </c:if>

                        </c:if>
                        <div class="rating-cart">
                            <div class="rating">
                                <c:forEach begin="1" end="5" var="i">
                                    <c:choose>
                                        <c:when test="${i <= product.rating}">
                                            <i class="fa-solid fa-star"></i>
                                        </c:when>
                                        <c:otherwise>
                                            <i class="fa-regular fa-star"></i>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </div>
                        </div>
                        <div class="bottom-info">
                            <span class="sold-count">Đã bán ${product.soldCount}</span>
                            <button class="cart-btn">
                                <i class="fa-solid fa-cart-plus"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </c:forEach>

        </div>
        <div class="view-all-btn">
            <c:choose>
                <c:when test="${product.category.id == 1}">
                    <a href="${pageContext.request.contextPath}/listproduct?categoryId=${product.category.id}"><i class="fa-solid fa-chevron-right"></i> Xem tất cả</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/listproduct_accessory?categoryId=${product.category.id}"><i class="fa-solid fa-chevron-right"></i> Xem tất cả</a>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</div>
<jsp:include page="/views/includes/footer.jsp"/>

<script>
    window.PRODUCT_DATA = {
        discount: ${product.discountPercentage},
        variantBasePrices: {},
        variantColorPrices: {},
        variantColorIds: {}
    };

    <c:forEach items="${variants}" var="v">
    window.PRODUCT_DATA.variantBasePrices[${v.id}] = ${v.basePrice};
    </c:forEach>

    <c:forEach items="${variantColors}" var="vc">
    window.PRODUCT_DATA.variantColorPrices["${vc.variantId}_${vc.colorId}"] = {
        price: ${vc.price},
        quantity: ${vc.quantity}
    };
    window.PRODUCT_DATA.variantColorIds["${vc.variantId}_${vc.colorId}"] = ${vc.id};
    </c:forEach>
</script>
<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/productDetail.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/cartCount.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/listProduct.js"></script>

</body>
</html>
