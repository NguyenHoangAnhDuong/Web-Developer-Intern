
const verifyStep = document.getElementById('verify-step');
const confirmStep = document.getElementById('confirm-step');

if (verifyStep) {
    const savedEmail = sessionStorage.getItem('resetEmail');
    const infoBold = verifyStep.querySelector('p b');
    if (infoBold) {
        infoBold.textContent = savedEmail;
    }

    const verifyBtn = document.getElementById('verify-btn');
    const confirmBtn = document.getElementById('confirm-btn');

    const resendLink = document.getElementById('resend-link');
    let resendTimerInterval = null;
    function formatTime(s) {
        const m = Math.floor(s / 60).toString().padStart(2, '0');
        const ss = (s % 60).toString().padStart(2, '0');
        return `${m}:${ss}`;
    }
    function setResendDisabled(remainingSec) {
        if (!resendLink) return;
        resendLink.classList.add('disabled');
        resendLink.setAttribute('aria-disabled', 'true');
        resendLink.style.pointerEvents = 'none';
        resendLink.textContent = `Gửi lại sau ${formatTime(remainingSec)}`;
        if (resendTimerInterval) clearInterval(resendTimerInterval);
        resendTimerInterval = setInterval(() => {
            remainingSec -= 1;
            if (remainingSec <= 0) {
                clearInterval(resendTimerInterval);
                resendTimerInterval = null;
                resendLink.classList.remove('disabled');
                resendLink.removeAttribute('aria-disabled');
                resendLink.style.pointerEvents = '';
                resendLink.textContent = 'Gửi lại email';
            } else {
                resendLink.textContent = `Gửi lại sau ${formatTime(remainingSec)}`;
            }
        }, 1000);
    }
    (function initResendState() {
        if (!resendLink) return;
        const createdAt = parseInt(sessionStorage.getItem('resetCodeCreatedAt') || '0', 10);
        if (!createdAt) {
            // không có mã -> link sẵn sàng
            resendLink.classList.remove('disabled');
            resendLink.removeAttribute('aria-disabled');
            resendLink.style.pointerEvents = '';
            resendLink.textContent = 'Gửi lại email';
            return;
        }
        const elapsed = Math.floor((Date.now() - createdAt) / 1000);
        const cooldown = 60; // 60 seconds
        if (elapsed < cooldown) {
            setResendDisabled(cooldown - elapsed);
        } else {
            // hết cooldown, cho bấm nhưng đừng tự động tạo mã mới
            resendLink.classList.remove('disabled');
            resendLink.removeAttribute('aria-disabled');
            resendLink.style.pointerEvents = '';
            resendLink.textContent = 'Gửi lại email';
        }
    })();
    if (resendLink) {
        resendLink.addEventListener('click', function (e) {
            e.preventDefault();
            // nếu đang trong cooldown, chặn (safety)
            if (resendLink.classList.contains('disabled')) return;
            const email = sessionStorage.getItem('resetEmail');
            if (!email) {
                alert('Không tìm thấy email. Vui lòng nhập lại email.');
                window.location.href = 'forgotpassword.html';
                return;
            }
            // Sinh mã mới và cập nhật createdAt
            const newCode = Math.floor(100000 + Math.random() * 900000).toString();
            sessionStorage.setItem('resetCode', newCode);
            sessionStorage.setItem('resetCodeCreatedAt', Date.now().toString());
            alert(`Mã xác nhận mới đã được gửi tới ${email}. Mã (thử): ${newCode}`);
            // Bắt đầu lại cooldown 60s
            setResendDisabled(60);
        });
    }
    // Nhập mã xác minh
    verifyBtn.addEventListener('click', function () {
        const codeInputs = document.querySelectorAll('#verify-step .code-inputs input');
        let code = '';
        codeInputs.forEach(input => code += input.value.trim());

        if (code.length < 6) {
            alert('Vui lòng nhập đủ 6 chữ số mã xác nhận!');
            return;
        }
        const storedCode = sessionStorage.getItem('resetCode');
        const createdAt = parseInt(sessionStorage.getItem('resetCodeCreatedAt') || '0', 10);
        // mã hết hạn sau 15 phút
        const expired = Date.now() - createdAt > 15 * 60 * 1000;
        if (!storedCode || expired) {
            alert('Mã xác nhận không hợp lệ hoặc đã hết hạn. Vui lòng gửi lại mã.');
            // xóa và quay về trang nhập email
            sessionStorage.removeItem('resetCode');
            sessionStorage.removeItem('resetCodeCreatedAt');
            window.location.href = 'forgotpassword.html';
            return;
        }

        if (code !== storedCode) {
            alert('Mã xác nhận không đúng. Vui lòng kiểm tra lại.');
            return;
        }

        // Xác minh thành công -> chuyển sang bước xác nhận
        verifyStep.classList.add('hidden');
        confirmStep.classList.remove('hidden');
    });

    // Bấm "Xác nhận" → sang bước nhập mật khẩu
    confirmBtn.addEventListener('click', function () {
        confirmStep.classList.add('hidden');
        window.location.href = 'formChangePass.html';
    });


    // Tự động focus qua ô kế tiếp khi nhập mã
    const codeInputs = document.querySelectorAll('#verify-step .code-inputs input');
    codeInputs.forEach((input, index) => {
        input.addEventListener('input', () => {
            if (input.value && index < codeInputs.length - 1) {
                codeInputs[index + 1].focus();
            }
        });
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && !input.value && index > 0) {
                codeInputs[index - 1].focus();
            }
        });
    });
}
