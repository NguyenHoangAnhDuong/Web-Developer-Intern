    <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
    <fmt:setLocale value="vi_VN"/>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Danh Sách Sản Phẩm - Điện Thoại</title>

        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/footer.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/listproduct.css">
    </head>

    <body>
    <jsp:include page="/views/includes/header.jsp"/>

    <main class="product-page">
        <!-- Thanh lọc & sắp xếp -->
        <div class="product-filter-bar">
            <!-- Hàng 1: Bộ lọc + thương hiệu -->
            <div class="filter-top">
                <div class="filter-btn">
                    <i class="fa-solid fa-filter"></i>
                    <span>Bộ lọc</span>
                </div>

                <div class="brand-list">
                    <div class="brand"><img src="assert/img/logoIphone.png" alt="iPhone"></div>
                    <div class="brand"><img src="assert/img/logoSamsung.png" alt="Samsung"></div>
                    <div class="brand"><img src="assert/img/logoOppo.png" alt="Oppo"></div>
                    <div class="brand"><img src="assert/img/logoVivo.png" alt="Vivo"></div>
                    <div class="brand"><img src="assert/img/logoRealme.png" alt="Realme"></div>
                    <div class="brand"><img src="assert/img/logoXiaomi.jpg" alt="Xiaomi"></div>
                    <div class="brand"><img src="assert/img/logoSony.png" alt="Sony"></div>
                    <div class="brand"><img src="assert/img/logoHonor.png" alt="Honor"></div>
                </div>
            </div>

            <!-- Hàng 2: Các nút lọc -->
            <div class="filter-options">
                <div class="filter-item" data-filter="gia">Giá </div>
                <div class="filter-item" data-filter="bonho">Bộ nhớ </div>
                <div class="filter-item" data-filter="mausac">Màu sắc </div>
                <div class="filter-item" data-filter="namramat">Năm ra mắt </div>
            </div>

            <!-- Menu phụ cho từng bộ lọc -->
            <div class="dropdown" id="gia">
                <h4 style="margin-top: 0">Hãy chọn mức giá phù hợp với bạn</h4>
                <div class="price-input">
                    <input type="number" placeholder="Từ">
                    <span>-</span>
                    <input type="number" placeholder="Đến">
                </div>
                <div class="dropdown-actions">
                    <button class="btn-close">Đóng</button>
                    <button class="btn-apply">Xem kết quả</button>
                </div>
            </div>

            <div class="dropdown" id="bonho">
                <div class="option-group">
                    <button>64GB</button>
                    <button>128GB</button>
                    <button>256GB</button>
                    <button>512GB</button>
                </div>
                <div class="dropdown-actions">
                    <button class="btn-close">Đóng</button>
                    <button class="btn-apply">Xem kết quả</button>
                </div>
            </div>

            <div class="dropdown" id="mausac">
                <div class="option-group">
                    <button>Đen</button>
                    <button>Trắng</button>
                    <button>Xanh</button>
                    <button>Hồng</button>
                    <button>Bạc</button>
                </div>
                <div class="dropdown-actions">
                    <button class="btn-close">Đóng</button>
                    <button class="btn-apply">Xem kết quả</button>
                </div>
            </div>

            <div class="dropdown" id="namramat">
                <div class="option-group">
                    <button>2021</button>
                    <button>2022</button>
                    <button>2023</button>
                    <button>2024</button>
                    <button>2025</button>
                </div>
                <div class="dropdown-actions">
                    <button class="btn-close">Đóng</button>
                    <button class="btn-apply">Xem kết quả</button>
                </div>
            </div>
        </div>

        <!-- Phần sắp xếp -->
        <div class="sort-section">
            <span>Sắp xếp theo:</span>
            <ul id="sortList">
                <li class="active">Nổi bật</li>
                <li>Bán chạy</li>
                <li>Giảm giá</li>
                <li>Mới</li>
                <li>Giá <i class="arrow"></i></li>
            </ul>
            <c:if test="${not empty param.sort or not empty param.priceMin or not empty param.priceMax or not empty param.brandId or not empty param.memory or not empty param.color or not empty param.year}">
                <a href="?" class="btn-reset-filters" title="Hoàn tác bộ lọc">
                    <i class="fa-solid fa-rotate-left"></i> Hoàn tác
                </a>
            </c:if>
        </div>
        </div>

        <!-- Danh sách sản phẩm -->
        <div id="product-list" class="product-list phone">
            <c:forEach var="product" items="${products}">
                <div class="product-card">
                    <a href="product-detail?id=${product.id}">
                        <div class="product-img">
                            <!--TỐI ƯU HÓA: Lazy loading ảnh -->
                            <img src="${product.image}"
                                 alt="${product.name}"
                                 loading="lazy">
                            <c:if test="${product.discount > 0}">
                                <span class="discount-badge">-${product.discount}%</span>
                            </c:if>
                        </div>
                    </a>
                    <div class="product-info">
                        <h2>${product.name}</h2>
                        <div class="price-wrap">
                            <span class="price-new" id="price-new-${product.id}"> <fmt:formatNumber value="${product.priceNew}" type="number" groupingUsed="true" pattern="#,###" />₫ </span>
                            <c:if test="${product.priceOld > product.priceNew}">
            <span class="price-old" id="price-old-${product.id}"> <fmt:formatNumber value="${product.priceOld}" type="number" groupingUsed="true" pattern="#,###" />₫
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
                                                    data-old-price="${variant.priceOld}"
                                                    data-product-id="${product.id}"> ${variant.name}
                                            </button>
                                            <c:set var="firstActive" value="false"/>
                                        </c:if>
                                    </c:forEach>
                                </div>
                                <c:if test="${not empty product.variants}">
                                    <div class="colors-selection">
                                        <span class="colors-label"></span>
                                        <div class="colors">
                                            <c:forEach var="color" items="${product.variants[0].colors}" varStatus="colorStatus">
                                                <button class="color ${colorStatus.first ? 'active' : ''}"
                                                        data-color="${color.name}"
                                                        data-color-id="${color.id}"
                                                        data-color-code="${color.code}"
                                                        data-variant-color-id="${color.id}"
                                                        data-color-price-new="${color.priceNew}"
                                                        data-color-price-old="${color.priceOld}"
                                                        title="${color.name}">
                                                </button>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:if>
                                <div class="variant-colors-data" style="display:none;">
                                    <c:forEach var="variant" items="${product.variants}" varStatus="variantStatus">
                                        <div class="variant-color-group" data-variant-id="${variant.id}">
                                            <c:forEach var="color" items="${variant.colors}">
                                                <span class="color-item" data-color="${color.name}" data-color-id="${color.id}" data-color-code="${color.code}" data-variant-color-id="${color.id}" data-color-price-old="${color.priceOld}" data-color-price-new="${color.priceNew}"></span>
                                            </c:forEach>
                                        </div>
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
                            <button class="cart-btn add-to-cart" data-product-id="${product.id}">
                                <i class="fa-solid fa-cart-plus"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <!-- Hiển thị số lượng sản phẩm -->
        <c:if test="${not empty totalItems}">
            <div class="product-count-info" style="margin: 20px 0; padding: 10px; background: #f5f5f5; border-radius: 5px;">
                <p style="margin: 0; color: #666;">
                    Hiển thị <strong>${(currentPage - 1) * pageSize + 1}</strong> -
                    <strong>${currentPage * pageSize > totalItems ? totalItems : currentPage * pageSize}</strong>
                    trong tổng số <strong>${totalItems}</strong> sản phẩm
                </p>
            </div>
        </c:if>

        <!-- Thông báo khi không có sản phẩm -->
        <c:if test="${empty products}">
            <div style="text-align: center; padding: 50px;">
                <p style="font-size: 18px; color: #999;">Không có sản phẩm nào</p>
            </div>
        </c:if>

        <!-- Phân trang -->
        <c:if test="${not empty totalPages and totalPages > 1}">
            <c:url var="baseUrl" value="">
                <c:if test="${not empty param.sort}">
                    <c:param name="sort" value="${param.sort}"/>
                </c:if>
                <c:if test="${not empty param.priceMin}">
                    <c:param name="priceMin" value="${param.priceMin}"/>
                </c:if>
                <c:if test="${not empty param.priceMax}">
                    <c:param name="priceMax" value="${param.priceMax}"/>
                </c:if>
                <c:if test="${not empty param.brandName}">
                    <c:param name="brandName" value="${param.brandName}"/>
                </c:if>
                <c:if test="${not empty paramValues.memory}">
                    <c:forEach var="mem" items="${paramValues.memory}">
                        <c:param name="memory" value="${mem}"/>
                    </c:forEach>
                </c:if>
                <c:if test="${not empty paramValues.color}">
                    <c:forEach var="col" items="${paramValues.color}">
                        <c:param name="color" value="${col}"/>
                    </c:forEach>
                </c:if>
                <c:if test="${not empty param.year}">
                    <c:param name="year" value="${param.year}"/>
                </c:if>
            </c:url>

            <!-- Tách query string từ URL -->
            <c:set var="queryString" value="${fn:substringAfter(baseUrl, '?')}"/>
            <c:if test="${not empty queryString}">
                <c:set var="queryString" value="&${queryString}"/>
            </c:if>

            <div class="pagination-container" style="margin: 30px 0; display: flex; justify-content: center; align-items: center; gap: 10px; flex-wrap: wrap;">
                <!-- Nút Trang trước -->
                <c:if test="${currentPage > 1}">
                    <a href="?page=${currentPage - 1}${queryString}"
                       class="pagination-btn" style="padding: 8px 16px; border: 1px solid #ddd; border-radius: 5px; text-decoration: none; color: #333; background: #fff; transition: all 0.3s;">
                        <i class="fa-solid fa-chevron-left"></i> Trước
                    </a>
                </c:if>

                <!-- Các số trang -->
                <c:set var="startPage" value="${currentPage > 3 ? currentPage - 2 : 1}" />
                <c:set var="endPage" value="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}" />

                <c:if test="${startPage > 1}">
                    <a href="?page=1${queryString}" class="pagination-btn" style="padding: 8px 16px; border: 1px solid #ddd; border-radius: 5px; text-decoration: none; color: #333; background: #fff;">1</a>
                    <c:if test="${startPage > 2}"><span style="padding: 8px 4px;">...</span></c:if>
                </c:if>

                <c:forEach var="i" begin="${startPage}" end="${endPage}">
                    <c:choose>
                        <c:when test="${i == currentPage}">
                            <span class="pagination-btn active" style="padding: 8px 16px; border: 1px solid var(--color-button); border-radius: 5px; background: var(--color-button); color: #fff; font-weight: bold;">${i}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="?page=${i}${queryString}"
                               class="pagination-btn" style="padding: 8px 16px; border: 1px solid #ddd; border-radius: 5px; text-decoration: none; color: #333; background: #fff; transition: all 0.3s;">${i}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <c:if test="${endPage < totalPages}">
                    <c:if test="${endPage < totalPages - 1}"><span style="padding: 8px 4px;">...</span></c:if>
                    <a href="?page=${totalPages}${queryString}" class="pagination-btn" style="padding: 8px 16px; border: 1px solid #ddd; border-radius: 5px; text-decoration: none; color: #333; background: #fff;">${totalPages}</a>
                </c:if>

                <!-- Nút Trang sau -->
                <c:if test="${currentPage < totalPages}">
                    <a href="?page=${currentPage + 1}${queryString}"
                       class="pagination-btn" style="padding: 8px 16px; border: 1px solid #ddd; border-radius: 5px; text-decoration: none; color: #333; background: #fff; transition: all 0.3s;">
                        Sau <i class="fa-solid fa-chevron-right"></i>
                    </a>
                </c:if>
            </div>
        </c:if>

    </main>
    <jsp:include page="/views/includes/footer.jsp"/>

    <!-- TỐI ƯU HÓA: Lazy loading script -->
    <script src="${pageContext.request.contextPath}/asset/js/lazyLoading.js"></script>
    <script src="${pageContext.request.contextPath}/asset/js/listProduct.js"></script>
    <script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
    </body>
    </html>