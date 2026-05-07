<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="isSuper" value="${sessionScope.roleName == 'super_admin'}" />
<c:set var="perms" value="${sessionScope.permissions}" />
<c:set var="canCreateProduct" value="${isSuper || (perms != null && perms.contains('product.create'))}" />
<c:set var="canUpdateProduct" value="${isSuper || (perms != null && perms.contains('product.update'))}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý sản phẩm - Admin</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sidebarAdmin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/productAdmin.css">
</head>

<body>
<%@ include file="/views/includes/toast.jsp" %>

<div class="app">

    <%@ include file="/views/includes/sideBarAdmin.jsp" %>

    <div class="container">

        <!-- TOP BAR -->
        <div class="topbar">
                <div class="muted">Quản lý sản phẩm</div>
        </div>
        <div class="page-content">
        <!-- TOOLBAR -->
        <div class="toolbar-wrapper">

            <form class="toolbar" method="get"
                  action="${pageContext.request.contextPath}/admin/products">

                <input type="text" name="keyword"
                       value="${keyword}"
                       placeholder="Tìm kiếm sản phẩm...">

                <select name="status">
                    <option value="">Tất cả trạng thái</option>
                    <option value="1" ${status == 1 ? "selected" : ""}>Đang hiển thị</option>
                    <option value="0" ${status == 0 ? "selected" : ""}>Đang ẩn</option>
                </select>

                <select name="categoryId">
                    <option value="">Tất cả danh mục</option>
                    <option value="1" ${categoryId == 1 ? "selected" : ""}>Điện thoại</option>
                    <option value="2" ${categoryId == 2 ? "selected" : ""}>Linh kiện</option>
                </select>

                <button class="btn-filter">Tìm kiếm</button>
            </form>

            <c:if test="${canCreateProduct}">
                <a class="btn-add"
                   href="${pageContext.request.contextPath}/admin/product/add">
                    + Thêm sản phẩm
                </a>
            </c:if>

        </div>

        <!-- TABLE -->
        <table class="table-view">
            <thead>
            <tr>
                <th>Hình ảnh</th>
                <th>Tên sản phẩm</th>
                <th>Danh mục</th>
                <th>Phiên bản</th>
                <th>Màu sắc</th>
                <th>Giá cơ bản</th>
                <th>Giá màu</th>
                <th>Tồn kho</th>
                <th>Hành động</th>
            </tr>
            </thead>

            <tbody>
            <c:forEach items="${products}" var="row">
                <tr>
                    <td>
                        <c:choose>
                            <c:when test="${row.c_id == 1}">
                                <img src="${pageContext.request.contextPath}/assert/img/product/${row.p_img}"
                                >
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}/assert/img/accesory/${row.p_img}"
                                >
                            </c:otherwise>
                        </c:choose>

                    </td>

                    <td class="text-left">${row.p_name}</td>

                    <td>${row.c_name}</td>

                    <td>${row.v_name}</td>

                    <td>${row.color_name}</td>

                    <td>
                        <fmt:formatNumber value="${row.base_price}" pattern="#,###"/>₫
                    </td>

                    <td>
                        <fmt:formatNumber value="${row.vc_price}" pattern="#,###"/>₫
                    </td>

                    <td>${row.quantity}</td>

                    <td class="actionsProduct">
                        <c:if test="${canUpdateProduct}">
                            <a class="btn-edit"
                               href="${pageContext.request.contextPath}/admin/products/edit?id=${row.vc_id}">
                                <i class="fa-solid fa-pencil"></i>
                            </a>

                            <button type="button"
                                    class="btn-toggle ajax-toggle"
                                    data-id="${row.vc_id}"
                                    title="${row.vc_status == 1 ? 'Ẩn' : 'Hiện'}">

                                <i class="fa-solid ${row.vc_status == 1 ? 'fa-eye' : 'fa-eye-slash'}"></i>

                            </button>
                        </c:if>
                    </td>

                </tr>
            </c:forEach>
            </tbody>
        </table>


            <div class="footer">
                <div class="pagination">

                    <c:set var="startPage" value="${currentPage - 2}" />
                    <c:set var="endPage" value="${currentPage + 2}" />

                    <c:if test="${startPage < 1}">
                        <c:set var="startPage" value="1"/>
                    </c:if>

                    <c:if test="${endPage > totalPages}">
                        <c:set var="endPage" value="${totalPages}"/>
                    </c:if>


                    <c:if test="${currentPage > 1}">
                        <a href="${pageContext.request.contextPath}/admin/products?page=${currentPage - 1}&keyword=${keyword}&status=${status}&categoryId=${categoryId}">
                            &laquo;
                        </a>
                    </c:if>


                    <c:forEach begin="${startPage}" end="${endPage}" var="i">
                        <c:choose>
                            <c:when test="${i == currentPage}">
                                <span class="active">${i}</span>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/admin/products?page=${i}&keyword=${keyword}&status=${status}&categoryId=${categoryId}">
                                        ${i}
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>


                    <c:if test="${currentPage < totalPages}">
                        <a href="${pageContext.request.contextPath}/admin/products?page=${currentPage + 1}&keyword=${keyword}&status=${status}&categoryId=${categoryId}">
                            &raquo;
                        </a>
                    </c:if>

                </div>
            </div>
        </div>
    </div>
</div>
<script>const contextPath = "${pageContext.request.contextPath}";</script>
<script src="${pageContext.request.contextPath}/asset/js/sidebarAdmin.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/productAdmin.js"></script>
</body>
</html>
