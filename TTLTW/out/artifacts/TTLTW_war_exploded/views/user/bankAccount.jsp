<%@ page import="vn.edu.hcmuaf.fit.ttltw.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
    Dữ liệu được set bởi BankAccountServlet
--%>
<html>
<head>
    <title>Title</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/accountSidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/info-user.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/paymentForm.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/header.css">
</head>

<body>
<jsp:include page="/views/includes/header.jsp"/>
<div id="pageWrapper">
    <jsp:include page="/views/includes/sidebarUser.jsp" />
    <div id="contentArea">
        <div class="bank-form" id="bankFormContainer">
            <h2>Tài Khoản Ngân Hàng</h2>

            <form id="bankForm" method="post" action="${pageContext.request.contextPath}/user/payment">

                <div class="field">
                    <label for="bankSelect">Tên ngân hàng</label>
                    <select id="bankSelect" name="bank_name">
                        <option value="Vietcombank" ${bankAccount != null && bankAccount.bankName == 'Vietcombank' ? 'selected' : ''}>Vietcombank</option>
                        <option value="VietinBank"  ${bankAccount != null && bankAccount.bankName == 'VietinBank' ? 'selected' : ''}>VietinBank</option>
                        <option value="Techcombank" ${bankAccount != null && bankAccount.bankName == 'Techcombank' ? 'selected' : ''}>Techcombank</option>
                        <option value="BIDV"        ${bankAccount != null && bankAccount.bankName == 'BIDV' ? 'selected' : ''}>BIDV</option>
                    </select>
                </div>

                <div class="field">
                    <label for="accountNumber">Số tài khoản</label>
                    <input type="text" id="accountNumber" name="account_number"
                           value="${bankAccount != null ? bankAccount.accountNumber : ''}">
                </div>

                <div class="field">
                    <label for="ownerName">Tên chủ tài khoản</label>
                    <input type="text" id="ownerName" name="account_name"
                           value="${bankAccount != null ? bankAccount.accountName : ''}">
                </div>

                <button type="button" id="btnBack" class="back">Sửa</button>
                <button type="submit" id="btnSubmit" class="submit">Hoàn Thành</button>

            </form>
        </div>
    </div>
</div>
<script src="${pageContext.request.contextPath}/asset/js/header.js"></script>
<script src="${pageContext.request.contextPath}/asset/js/paymentForm.js?v=3"></script>
<script src="${pageContext.request.contextPath}/asset/js/info-user.js"></script>
</body>
</html>
