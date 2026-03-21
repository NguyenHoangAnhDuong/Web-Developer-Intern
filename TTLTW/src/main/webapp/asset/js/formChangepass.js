// Cập nhật mật khẩu mới
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_\-#^()])[A-Za-z\d@$!%*?&_\-#^()]{8,}$/;
function validateNewPassword(password) {
    if (!password || password.length === 0)
        return 'Vui lòng nhập mật khẩu mới!';
    if (password.length < 8)
        return 'Mật khẩu phải có ít nhất 8 ký tự!';
    if (!PASSWORD_REGEX.test(password))
        return 'Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt!';
    return null;
}
document.getElementById('reset-form').addEventListener('submit', function (e) {
    const oldPass     = document.getElementById('old-pass').value.trim();
    const newPass     = document.getElementById('new-pass').value.trim();
    const confirmPass = document.getElementById('confirm-pass').value.trim();
    // Kiểm tra rỗng
    if (!oldPass) {
        e.preventDefault();
        showToast('Vui lòng nhập mật khẩu cũ!', 'error');
        return;
    }
    // Kiểm tra định dạng mật khẩu mới
    const passError = validateNewPassword(newPass);
    if (passError) {
        e.preventDefault();
        showToast(passError, 'error');
        return;
    }
    // Kiểm tra xác nhận khớp
    if (newPass !== confirmPass) {
        e.preventDefault();
        showToast('Mật khẩu xác nhận không khớp!', 'error');
        return;
    }
    // Kiểm tra mật khẩu mới không trùng mật khẩu cũ
    if (oldPass === newPass) {
        e.preventDefault();
        showToast('Mật khẩu mới không được trùng mật khẩu cũ!', 'error');
        return;
    }
});
function setFieldState(input, isValid) {
    const wrapper = input.closest('.input-group');
    let icon = wrapper.querySelector('.field-icon');

    // Tạo icon nếu chưa có
    if (!icon) {
        icon = document.createElement('span');
        icon.className = 'field-icon';
        input.parentElement.appendChild(icon);
    }

    if (isValid) {
        input.style.borderColor = '#2ecc71';
        icon.innerHTML = '<i class="fa-solid fa-circle-check" style="color:#2ecc71"></i>';
    } else {
        input.style.borderColor = '#e74c3c';
        icon.innerHTML = '<i class="fa-solid fa-circle-xmark" style="color:#e74c3c"></i>';
    }
}

function clearFieldState(input) {
    input.style.borderColor = '';
    const wrapper = input.closest('.input-group');
    const icon = wrapper.querySelector('.field-icon');
    if (icon) icon.innerHTML = '';
}
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
    document.getElementById('confirm-pass').addEventListener('input', function () {
        const newPass     = document.getElementById('new-pass').value.trim();
        const confirmPass = this.value.trim();

        if (confirmPass && newPass !== confirmPass) {
            this.style.borderColor = '#e74c3c';
        } else {
            this.style.borderColor = '';
        }
    });
});
