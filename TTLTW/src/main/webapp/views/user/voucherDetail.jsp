<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết voucher</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/voucherDetail.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/accountSidebar.css">
</head>
<body>
<jsp:include page="../includes/header.jsp"/>

<div class="voucher-container page-wrapper">
    <jsp:include page="../includes/sidebarUser.jsp"/>

    <div class="voucher-list">
        <c:forEach var="voucher" items="${listVoucher}">
            <div class="voucher ${voucher.status == 0 ? 'expired' : ''}">
                <div class="voucher-left">
                    <div class="icon">
                        <img src="${pageContext.request.contextPath}/asset/img/logo.png" alt="logo">
                    </div>
                </div>
                <div class="voucher-right">
                    <div>
                        <c:choose>
                            <c:when test="${voucher.type == 1}">
                                <h3>
                                    Giảm ${voucher.discountAmount}%
                                    <span style="font-size:14px; font-weight:400;">
                        (Tối đa <fmt:formatNumber value="${voucher.maxReduce}" type="number"/> ₫)
                    </span>
                                </h3>
                            </c:when>

                            <c:otherwise>
                                <h3>Giảm <fmt:formatNumber value="${voucher.discountAmount}" type="number"/> ₫</h3>
                            </c:otherwise>
                        </c:choose>

                        <p>Đơn tối thiểu <fmt:formatNumber value="${voucher.minOrderValue}" type="number"/> ₫</p>
                    </div>
                </div>

                <c:if test="${voucher.status != 0}">
                    <div class="badge">x${voucher.quantity}</div>
                </c:if>
            </div>
        </c:forEach>
    </div>
</div>


<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
<script>
    const menuAccountMain = document.getElementById("menuAccountMain");
    const accountSubmenu = document.getElementById("accountSubmenu");

    if (menuAccountMain && accountSubmenu) {
        accountSubmenu.classList.add("open");
        menuAccountMain.addEventListener("click", (e) => {
            e.preventDefault();
            accountSubmenu.classList.toggle("open");
        });
    }
</script>
</body>
</html>
