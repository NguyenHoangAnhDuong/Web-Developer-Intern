document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".form-regis");
    if (!form) return;

    const fields = {
        fname:    form.querySelector('#fname'),
        lname:    form.querySelector('#lname'),
        email:    form.querySelector('#email'),
        username: form.querySelector('#username'),
        password: form.querySelector('#password'),
        confirm:  form.querySelector('#confirm'),
    };
    const termsCheckbox = form.querySelector('input[name="terms"]');

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    const usernameRegex = /^[a-zA-Z0-9_]+$/;
    const strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;

    function setError(name, message) {
        const input = fields[name] || (name === 'terms' ? termsCheckbox : null);
        const slot = form.querySelector('.field-error[data-for="' + name + '"]');
        if (slot) slot.textContent = message || '';
        if (input) {
            const group = input.closest('.input-group') || input.closest('.terms');
            if (group) group.classList.toggle('has-error', !!message);
        }
    }

    function clearError(name) { setError(name, ''); }

    function validateField(name) {
        const input = fields[name];
        if (name === 'terms') {
            if (termsCheckbox && !termsCheckbox.checked) {
                setError('terms', 'Bạn cần đồng ý với điều khoản dịch vụ để tiếp tục.');
                return false;
            }
            clearError('terms');
            return true;
        }
        if (!input) return true;
        const value = (input.value || '').trim();

        switch (name) {
            case 'fname':
                if (!value) return setError('fname', 'Vui lòng nhập họ.'), false;
                if (value.length > 50) return setError('fname', 'Họ không được vượt quá 50 ký tự.'), false;
                break;
            case 'lname':
                if (!value) return setError('lname', 'Vui lòng nhập tên.'), false;
                if (value.length > 50) return setError('lname', 'Tên không được vượt quá 50 ký tự.'), false;
                break;
            case 'email':
                if (!value) return setError('email', 'Vui lòng nhập email.'), false;
                if (!emailRegex.test(value)) return setError('email', 'Email không đúng định dạng.'), false;
                break;
            case 'username':
                if (!value) return setError('username', 'Vui lòng nhập tên đăng nhập.'), false;
                if (value.length < 4 || value.length > 30)
                    return setError('username', 'Tên đăng nhập phải từ 4 đến 30 ký tự.'), false;
                if (!usernameRegex.test(value))
                    return setError('username', 'Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới.'), false;
                break;
            case 'password':
                if (!value) return setError('password', 'Vui lòng nhập mật khẩu.'), false;
                if (!strongPasswordRegex.test(value))
                    return setError('password',
                        'Mật khẩu tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&).'), false;
                // Khi password thay đổi, validate lại confirm
                if (fields.confirm.value) validateField('confirm');
                break;
            case 'confirm':
                if (!value) return setError('confirm', 'Vui lòng xác nhận mật khẩu.'), false;
                if (value !== fields.password.value)
                    return setError('confirm', 'Mật khẩu xác nhận không khớp.'), false;
                break;
        }
        clearError(name);
        return true;
    }

    // Validate khi rời ô; xoá lỗi inline ngay khi user gõ lại
    Object.keys(fields).forEach(function (name) {
        const input = fields[name];
        if (!input) return;
        input.addEventListener('blur', function () { validateField(name); });
        input.addEventListener('input', function () {
            // Chỉ xoá lỗi để user không bị "đỏ" khi vừa gõ
            clearError(name);
        });
    });

    if (termsCheckbox) {
        termsCheckbox.addEventListener('change', function () { validateField('terms'); });
    }

    form.addEventListener('submit', function (e) {
        // Validate toàn bộ form trước khi submit
        let ok = true;
        ['fname', 'lname', 'email', 'username', 'password', 'confirm', 'terms']
            .forEach(function (name) { if (!validateField(name)) ok = false; });

        if (!ok) {
            e.preventDefault();
            // Focus ô đầu tiên bị lỗi
            const firstError = form.querySelector('.input-group.has-error input, .terms.has-error input');
            if (firstError) firstError.focus();
        }
    });
});
