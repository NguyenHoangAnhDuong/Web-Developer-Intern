document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById('reset-form');
    if (!form) return;

    const newPass     = document.getElementById('new-pass');
    const confirmPass = document.getElementById('confirm-pass');
    const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;

    function validateNewPassword(password) {
        if (!password || password.length === 0)
            return 'Vui lòng nhập mật khẩu mới!';
        if (!PASSWORD_REGEX.test(password))
            return 'Mật khẩu tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&).';
        return null;
    }

    form.addEventListener('submit', function (e) {
        const np = newPass.value.trim();
        const cp = confirmPass.value.trim();

        const passError = validateNewPassword(np);
        if (passError) {
            e.preventDefault();
            showToast(passError, 'error');
            newPass.focus();
            return;
        }
        if (np !== cp) {
            e.preventDefault();
            showToast('Mật khẩu xác nhận không khớp!', 'error');
            confirmPass.focus();
        }
    });

    // Hiển thị trạng thái khớp/khớp cho ô confirm
    confirmPass.addEventListener('input', function () {
        const np = newPass.value.trim();
        const cp = this.value.trim();
        this.style.borderColor = (cp && np !== cp) ? '#e74c3c' : '';
    });

    // Ẩn/hiện mật khẩu (icon mắt) — cùng pattern với formChangepass.js
    document.querySelectorAll('.toggle').forEach(toggle => {
        const eye      = toggle.querySelector('.fa-eye');
        const eyeSlash = toggle.querySelector('.fa-eye-slash');
        if (eye) eye.style.display = 'inline';
        if (eyeSlash) eyeSlash.style.display = 'none';
        toggle.addEventListener('click', () => {
            const input = toggle.parentElement.querySelector('input');
            if (!input) return;
            if (input.type === 'password') {
                input.type = 'text';
                if (eye) eye.style.display = 'none';
                if (eyeSlash) eyeSlash.style.display = 'inline';
            } else {
                input.type = 'password';
                if (eye) eye.style.display = 'inline';
                if (eyeSlash) eyeSlash.style.display = 'none';
            }
        });
    });
});
