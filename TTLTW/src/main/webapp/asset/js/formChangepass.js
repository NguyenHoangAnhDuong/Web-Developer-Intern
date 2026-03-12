// Cập nhật mật khẩu mới
const updateBtn = document.getElementById('update-btn');
updateBtn.addEventListener('click', function () {
    const newPassInput = document.getElementById('new-pass');
    const confirmPassInput = document.getElementById('confirm-pass');
    const newPass = newPassInput ? newPassInput.value.trim() : '';
    const confirmPass = confirmPassInput ? confirmPassInput.value.trim() : '';
    if (!newPass || !confirmPass) {
        alert('Vui lòng nhập đầy đủ thông tin!');
        return;
    }
    if (newPass.length < 6) {
        alert('Mật khẩu mới cần ít nhất 6 ký tự.');
        return;
    }
    if (newPass !== confirmPass) {
        alert('Mật khẩu xác nhận không khớp!');
        return;
    }
    document.getElementById('reset-form').submit();
});

// Ẩn / hiện mật khẩu (icon mắt)
document.querySelectorAll('.toggle').forEach(toggle => {
    const eye = toggle.querySelector('.fa-eye');
    const eyeSlash = toggle.querySelector('.fa-eye-slash');
    if (eye) eye.style.display = 'inline';
    if (eyeSlash) eyeSlash.style.display = 'none';
    toggle.addEventListener('click', () => {
        const input = toggle.parentElement.querySelector('input');
        const eye = toggle.querySelector('.fa-eye');
        const eyeSlash = toggle.querySelector('.fa-eye-slash');
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
