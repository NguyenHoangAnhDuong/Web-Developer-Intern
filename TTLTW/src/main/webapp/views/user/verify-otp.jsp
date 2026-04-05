<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác thực OTP</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/sendotp.css">
</head>
<body>


<c:if test="${not empty sessionScope.toastMessage}">
    <div class="toast ${sessionScope.toastType}" id="toast">${sessionScope.toastMessage}</div>
    <c:remove var="toastMessage" scope="session"/>
    <c:remove var="toastType"    scope="session"/>
</c:if>

<div class="otp-card">
    <h2>Xác thực email</h2>
    <p class="subtitle">
        Mã OTP gồm 6 chữ số đã được gửi đến<br>
        <strong>${maskedEmail}</strong><br>
        Mã có hiệu lực trong <strong>5 phút</strong>.
    </p>

    <c:if test="${not empty error}">
        <div class="error-message">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/verify-otp">
        <input type="text"
               name="otp"
               class="otp-input"
               maxlength="6"
               placeholder="_ _ _ _ _ _"
               autocomplete="one-time-code"
               inputmode="numeric"
               autofocus
               required>

        <p class="timer">Mã hết hạn sau: <span id="countdown">05:00</span></p>

        <button type="submit" class="btn-submit">Xác nhận</button>
    </form>

    <div class="resend-area">
        Không nhận được mã?
        <form method="post" action="${pageContext.request.contextPath}/verify-otp" style="display:inline">
            <input type="hidden" name="action" value="resend">
            <button type="submit" id="resend-btn" disabled>Gửi lại (<span id="resend-timer">60</span>s)</button>
        </form>
    </div>

    <a class="back-link" href="${pageContext.request.contextPath}/register">← Quay lại đăng ký</a>
</div>

<script>
    //Countdown 5 phút
    let total = 300; 
    const countdownEl = document.getElementById('countdown');

    const countdownTimer = setInterval(() => {
        total--;
        const m = String(Math.floor(total / 60)).padStart(2, '0');
        const s = String(total % 60).padStart(2, '0');
        countdownEl.textContent = m + ':' + s;
        if (total <= 0) {
            clearInterval(countdownTimer);
            countdownEl.textContent = '00:00';
            countdownEl.style.color = '#aaa';
        }
    }, 1000);

    // Nút gửi lại sẽ được kích hoạt sau 60s
    let resendSec = 60;
    const resendBtn   = document.getElementById('resend-btn');
    const resendTimer = document.getElementById('resend-timer');

    const resendInterval = setInterval(() => {
        resendSec--;
        resendTimer.textContent = resendSec;
        if (resendSec <= 0) {
            clearInterval(resendInterval);
            resendBtn.disabled = false;
            resendBtn.textContent = 'Gửi lại';
        }
    }, 1000);

    // Chỉ cho nhập số và tối đa 6 ký tự
    document.querySelector('.otp-input').addEventListener('input', function () {
        this.value = this.value.replace(/\D/g, '').substring(0, 6);
    });

    // Hiện toast
    const toast = document.getElementById('toast');
    if (toast) {
        setTimeout(() => toast.classList.add('show'), 100);
        setTimeout(() => toast.classList.remove('show'), 3500);
    }
</script>
</body>
</html>
