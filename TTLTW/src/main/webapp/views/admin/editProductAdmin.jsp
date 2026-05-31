<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chỉnh sửa sản phẩm</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/editProduct.css">
</head>
<body>
<jsp:include page="/views/includes/toast.jsp"/>
<c:set var="isPhone" value="${product.category_id == 1}" />
<div class="container">

    <div class="header-section">
        <h2>Chỉnh sửa chi tiết phiên bản</h2>
        <span class="product-tag">
            ${isPhone ? "Điện thoại" : "Linh kiện"}
        </span>
    </div>

    <div class="product-switch">
        <button type="button" class="${isPhone ? 'active' : ''}">📱 Điện thoại</button>
        <button type="button" class="${!isPhone ? 'active' : ''}">🔌 Linh kiện</button>
    </div>


    <c:if test="${isPhone}">
        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/products/edit">

            <input type="hidden" name="productId" value="${product.product_id}">
            <input type="hidden" name="categoryId" value="1">
            <input type="hidden" name="currentImage" value="${product.product_img}">

            <div class="card">
                <div class="form-grid">
                    <div class="form-group">
                        <label>Tên sản phẩm</label>
                        <input type="text"
                               name="productName"
                               value="${product.product_name}"
                               readonly
                               class="readonly-input">
                    </div>

                    <div class="image-management">
                        <div class="form-group image-box">
                            <div class="current-image-box">
                                <c:choose>
                                    <c:when test="${not empty product.product_img and fn:startsWith(product.product_img, 'http')}">
                                        <img src="${product.product_img}" alt="${product.product_name}">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}/${product.product_img}" alt="${product.product_name}">
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div class="upload-action">
                            <label>Thay đổi hình ảnh</label>
                            <input type="file" name="image">
                        </div>
                    </div>

                    <div class="form-group full">
                        <label>Mô tả tổng quát</label>
                        <textarea name="description" rows="3">
                                ${product.description}
                        </textarea>
                    </div>
                </div>
            </div>


            <div class="card">
                <h3>  📊 Thông số kỹ thuật</h3>

                <div class="tech-specs-list">
                    <div class="tech-row header">
                        <span class="col-name">Tên thông số</span>
                        <span class="col-value">Giá trị</span>
                        <span class="col-priority">Ưu tiên</span>
                        <span></span>
                    </div>

                    <c:forEach items="${product.techs}" var="t">
                    <div class="tech-row">
                            <input name="techNames[]" class="col-name" value="${t.tech_name}">
                            <input name="techValues[]" class="col-value" value="${t.tech_value}">
                            <input name="techPriorities[]" class="col-priority" type="number" value="${t.priority}">
                            <button type="button" class="btn-remove-tech">✕</button>
                        </div>
                    </c:forEach>
                </div>

                <button type="button" class="btn-add-minor">+ Thêm thông số</button>
            </div>


            <div class="card highlight-card">
                <h3>⚙️ ${product.variant_name}</h3>

                <input type="hidden" name="variantId" value="${product.variant_id}">
                <input type="hidden" name="colorId" value="${product.color_id}">

                <div class="form-grid">
                    <div class="form-group">
                        <label>Tên phiên bản</label>
                        <input name="variantName" value="${product.variant_name}">
                    </div>

                    <div class="form-group">
                        <label>Giá phiên bản (Cơ bản)</label>
                        <input name="basePrice" type="number" value="${product.base_price}">
                    </div>
                    <div class="form-group">
                        <label>Bảo hành (tháng)</label>
                        <input name="warranty" type="number" value="${product.warranty}">
                    </div>
                    <div class="form-group">
                    <label>Giá theo màu</label>
                    <input name="colorPrice" type="number" value="${product.color_price}">
                </div>
                    <div class="form-group">
                        <label>Giảm giá (%)</label>
                        <input type="number"
                               name="discountPercentage"
                               min="0"
                               max="100"
                               value="${product.discount_percentage}">
                    </div>

                </div>

                <hr class="dashed">

                <div class="color-edit-row">
                    <div class="input-unit">
                        <span>Màu sắc</span>
                        <input value="${product.color_name}" readonly class="readonly-input">
                    </div>

                    <div class="input-unit">
                        <span>Tồn kho</span>
                        <input name="quantity" type="number" value="${product.quantity}">
                    </div>

                    <div class="input-unit">
                        <span>SKU</span>
                        <input name="sku" value="${product.sku}" class="sku-input">
                    </div>
                </div>
            </div>

            <div class="action-buttons">
                <button type="button" class="btn-cancel" onclick="history.back()">HỦY</button>
                <button type="submit" class="btn-save">LƯU</button>
            </div>
        </form>
    </c:if>

    <c:if test="${not isPhone}">
        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/products/edit">

            <input type="hidden" name="productId" value="${accessory.product_id}">
            <input type="hidden" name="categoryId" value="2">
            <input type="hidden" name="currentImage" value="${accessory.product_img}">

            <div class="card">
                <div class="form-grid">
                    <div class="form-group">
                        <label>Tên linh kiện</label>
                        <input type="text" name="productName" value="${accessory.product_name}" readonly class="readonly-input">
                    </div>

                    <div class="image-management">
                        <div class="form-group image-box">
                            <div class="current-image-box">
                                <c:choose>
                                    <c:when test="${not empty accessory.product_img and fn:startsWith(accessory.product_img, 'http')}">
                                        <img src="${accessory.product_img}" alt="Linh kiện">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}/${accessory.product_img}" alt="Linh kiện">
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="upload-action">
                            <label>Thay đổi hình ảnh</label>
                            <input type="file" name="image">
                        </div>
                    </div>

                    <div class="form-group full">
                        <label>Mô tả tổng quát</label>
                        <textarea name="description" rows="3">${accessory.description}</textarea>
                    </div>
                </div>
            </div>

            <div class="card">
                <h3>📊 Thông số kỹ thuật</h3>
                <div class="tech-specs-list">
                    <div class="tech-row header">
                        <span class="col-name">Tên thông số</span>
                        <span class="col-value">Giá trị</span>
                        <span class="col-priority">Ưu tiên</span>
                        <span></span>
                    </div>
                    <c:forEach items="${accessory.techs}" var="t">
                        <div class="tech-row">
                            <input name="techNames[]" class="col-name" value="${t.tech_name}">
                            <input name="techValues[]" class="col-value" value="${t.tech_value}">
                            <input name="techPriorities[]" class="col-priority" type="number" value="${t.priority}">
                            <button type="button" class="btn-remove-tech">✕</button>
                        </div>
                    </c:forEach>
                </div>
                <button type="button" class="btn-add-minor">+ Thêm thông số</button>
            </div>

            <div class="card highlight-card">
                <h3>⚙️ ${accessory.variant_name}</h3>

                <input type="hidden" name="variantIds[]" value="${accessory.variant_id}">
                <input type="hidden" name="colorIds[]" value="${accessory.color_id}">

                <div class="form-grid">

                    <div class="form-group">
                        <label>Tên phiên bản</label>
                        <input name="variantNames[]" value="${accessory.variant_name}">
                    </div>

                    <div class="form-group">
                        <label>Giá bán</label>
                        <input type="number" name="colorPrices[]" value="${accessory.color_price}">
                    </div>

                    <div class="form-group">
                        <label>Giảm giá (%)</label>
                        <input type="number"
                               name="discountPercentage"
                               min="0"
                               max="100"
                               value="${accessory.discount_percentage}">
                    </div>

                </div>

                <div class="color-edit-row">

                    <div class="input-unit">
                        <span>Màu sắc</span>
                        <input value="${accessory.color_name}" readonly class="readonly-input">
                    </div>

                    <div class="input-unit">
                        <span>Tồn kho</span>
                        <input type="number" name="variantQuantities[]" value="${accessory.quantity}">
                    </div>

                </div>
            </div>

            <div class="action-buttons">
                <button type="button" class="btn-cancel" onclick="history.back()">HỦY</button>
                <button type="submit" class="btn-save">LƯU</button>
            </div>
        </form>
    </c:if>

</div>

<script src="${pageContext.request.contextPath}/asset/js/editProduct.js"></script>
</body>
</html>
