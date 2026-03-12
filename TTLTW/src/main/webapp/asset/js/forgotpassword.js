var forgotForm = document.querySelector('#forgot-password form');
if (forgotForm) {
    forgotForm.addEventListener('submit', function (e) {
        e.preventDefault();
        const emailInput = forgotForm.querySelector('input[type="email"]');
        const email = emailInput.value.trim();
        if (!email) {
            alert("Vui lòng nhập email của bạn!");
            return;
        }
        const emailValue = emailInput.value.trim();
        // Regex kiểm tra email hợp lệ
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(emailValue)) {
            e.preventDefault();
            alert("Email không hợp lệ! Vui lòng nhập đúng định dạng.");
            emailInput.focus();
            return;
        }

        const code = Math.floor(100000 + Math.random() * 900000).toString();
        sessionStorage.setItem('resetEmail', email);
        sessionStorage.setItem('resetCode', code);
        sessionStorage.setItem('resetCodeCreatedAt', Date.now().toString());

        // Giả lập gửi email — để tiện test hiển thị luôn mã trong alert (bỏ phần này khi chạy thực tế)
        alert(`Mã xác nhận đã được gửi tới ${email}. Mã (thử): ${code}`);

        // Chuyển sang trang nhập mã (lưu ý: file tên hiện tại là changepassword.html)
        window.location.href = 'changepassword.html';
    });
}