document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("forgot-form");
    if (!form) return;

    const emailInput = form.querySelector("#email");
    const errorSlot  = form.querySelector('.field-error[data-for="email"]');
    const group      = emailInput.closest('.input-group');
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

    function setError(msg) {
        if (errorSlot) errorSlot.textContent = msg || '';
        if (group) group.classList.toggle('has-error', !!msg);
    }

    emailInput.addEventListener('input', function () { setError(''); });

    form.addEventListener('submit', function (e) {
        const value = (emailInput.value || '').trim();
        if (!value) {
            e.preventDefault();
            setError('Vui lòng nhập email.');
            emailInput.focus();
            return;
        }
        if (!emailRegex.test(value)) {
            e.preventDefault();
            setError('Email không đúng định dạng.');
            emailInput.focus();
        }
    });
});
