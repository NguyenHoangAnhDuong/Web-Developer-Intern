<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm sản phẩm</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/addProductAdmin.css">
</head>
<body>
<jsp:include page="/views/includes/toast.jsp"/>
<div class="add-product">
    <div class="container">

        <h2>Thêm sản phẩm</h2>

        <!-- CHỌN LOẠI -->
        <div class="type-select">
            <button type="button" onclick="showPhone()">Điện thoại</button>
            <button type="button" onclick="showPart()">Linh kiện</button>
        </div>

        <!-- ===== FORM ĐIỆN THOẠI ===== -->
        <form id="phoneForm"
              class="form hidden"
              method="post"
              action="${pageContext.request.contextPath}/admin/product/add"
              enctype="multipart/form-data">
            <!-- NOTE: BỔ SUNG categoryId -->
            <input type="hidden" name="categoryId" value="1">

        <h3>Điện thoại</h3>

            <label>Ảnh đại diện</label>
            <input type="file" name="productImage" accept="image/*" required>

            <input name="productName" placeholder="Tên sản phẩm" required>
            <input
                    type="number"
                    name="discountPercentage"
                    placeholder="Giảm giá (%)"
                    min="0"
                    max="100"
                    value="0">

            <div class="brand-row">
                <select name="brandId" onchange="toggleBrand(this)" required>
                    <option value="">-- Chọn hãng --</option>
                    <option value="1">Apple</option>
                    <option value="2">Samsung</option>
                    <option value="3">Xiaomi</option>
                    <option value="custom">Khác...</option>
                </select>

                <input type="text"
                       name="customBrand"
                       placeholder="Nhập hãng mới"
                       style="display:none">

            </div>

            <textarea name="description" placeholder="Mô tả"></textarea>

            <!-- THÔNG SỐ -->
            <h4>Thông số</h4>
            <div id="phoneTech"></div>
            <button type="button" onclick="addTech('phoneTech')">Thêm thông số</button>

            <!-- PHIÊN BẢN -->
            <h4>Phiên bản</h4>
            <div id="phoneVariant"></div>
            <button type="button" onclick="addVariant('phoneVariant')">Thêm phiên bản</button>

            <button type="submit">Lưu điện thoại</button>
        </form>

        <!-- ===== FORM LINH KIỆN ===== -->
        <form id="partForm"
              class="form hidden"
              method="post"
              action="${pageContext.request.contextPath}/admin/product/add"
              enctype="multipart/form-data">
            <input type="hidden" name="categoryId" value="2">

        <h3>Linh kiện</h3>

            <label>Ảnh đại diện</label>
            <input type="file" name="productImage" accept="image/*" required>

            <input name="productName" placeholder="Tên linh kiện" required>
            <input
                    type="number"
                    name="discountPercentage"
                    placeholder="Giảm giá (%)"
                    min="0"
                    max="100"
                    value="0">

            <div class="brand-row">
                <select name="brandId" onchange="toggleBrand(this)" required>
                    <option value="">-- Chọn hãng --</option>
                    <option value="1">Apple</option>
                    <option value="2">Samsung</option>
                    <option value="3">Xiaomi</option>
                    <option value="custom">Khác...</option>
                </select>

                <input type="text"
                       name="customBrand"
                       placeholder="Nhập hãng mới"
                       style="display:none">

            </div>

            <select name="subcategory">
                <option>Pin</option>
                <option>Màn hình</option>
            </select>

            <!-- THÔNG SỐ -->
            <h4>Thông số</h4>
            <div id="partTech"></div>
            <button type="button" onclick="addTech('partTech')">Thêm thông số</button>

            <!-- PHIÊN BẢN -->
            <h4>Phiên bản</h4>
            <div id="partVariant"></div>
            <button type="button" onclick="addVariant('partVariant')">Thêm phiên bản</button>

            <button type="submit">Lưu linh kiện</button>
        </form>

    </div>
</div>

<!-- ===== TEMPLATE ===== -->

<template id="techTpl">
    <div class="tech">
        <!-- NOTE: thêm [] -->
        <input name="techName[]" placeholder="Tên thông số">
        <input name="techValue[]" placeholder="Giá trị">
        <input name="techPriority[]" value="5">
        <button type="button" onclick="removeBlock(this)">✖</button>
    </div>
</template>

<template id="phoneVariantTpl">
    <div class="variant">
        <input name="variantName[]" placeholder="Tên phiên bản" required>
        <input type="number" name="basePrice[]" placeholder="Giá gốc" min="1000" step="100" required>

        <div class="colors"></div> <button type="button" class="btn-add-color" onclick="addColor(this)">Thêm màu</button>
        <button type="button" class="danger" onclick="removeBlock(this)">Xóa phiên bản</button>
    </div>
</template>

<template id="partVariantTpl">
    <div class="variant">
        <input name="variantName[]" placeholder="Tên phiên bản" required>
        <input type="number" name="basePrice[]" placeholder="Giá gốc" min="0.01" step="0.01" required>

        <input name="variantQuantity[]" placeholder="Số lượng tồn kho" required>

        <div class="colors" style="display:none"></div> <button type="button" class="danger" onclick="removeBlock(this)">Xóa phiên bản</button>
    </div>
</template>

<template id="colorTpl">
    <div class="color">
        <input type="hidden" name="colorVariantIndex[]" class="variant-index-input">
        <div class="color-row">
            <select name="colorId[]" onchange="toggleColor(this)" required>
                <option value="">-- Chọn màu --</option>
                <option value="1">Đen</option>
                <option value="2">Trắng</option>
                <option value="3">Xanh</option>
                <option value="4">Tím</option>
                <option value="5">Đỏ Lựu</option>
                <option value="6">Xám</option>
                <option value="custom">Khác...</option>
            </select>
            <input type="text" name="customColor[]" placeholder="Nhập màu mới" style="display:none">
        </div>
        <input type="number" name="colorPrice[]" placeholder="Giá màu" value="" min="0.01" step="0.01" required>
        <input type="number" name="quantity[]" placeholder="Số lượng" value="" min="0" step="1" required>
        <input name="sku[]" placeholder="SKU">
        <input type="file" multiple class="color-image-input" accept="image/*">
        <button type="button" onclick="removeBlock(this)">✖</button>
    </div>
</template>
<c:if test="${param.status == 'success'}">
    <script>
        alert("Thêm sản phẩm thành công!");
    </script>
</c:if>
<script src="${pageContext.request.contextPath}/asset/js/addProductAdmin.js"></script>

</body>
</html>
