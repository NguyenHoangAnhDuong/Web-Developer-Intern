document.addEventListener("DOMContentLoaded", function () {
    const registerForm = document.querySelector(".form-regis");
    const username = document.getElementById("username");
    const password = document.getElementById("password");
    const confirmPass = document.getElementById("confirm");
    const email = document.getElementById("email");
    const firstName = document.getElementById("fname");
    const lastName = document.getElementById("lname");
    const errorMessageDiv = document.getElementById("error-message");

    registerForm.addEventListener("submit", function (e) {
        e.preventDefault(); // Ngăn reload trang

        const usernameVal = username.value.trim();
        const passwordVal = password.value.trim();
        const confirmVal = confirmPass.value.trim();
        const emailVal = email.value.trim();
        const firstVal = firstName.value.trim();
        const lastVal = lastName.value.trim();

        errorMessageDiv.textContent = "";

        if (!usernameVal || !passwordVal || !emailVal || !firstVal || !lastVal) {
            errorMessageDiv.textContent = "Vui lòng nhập đầy đủ thông tin!";
            return;
        }

        if (passwordVal !== confirmVal) {
            errorMessageDiv.textContent = "Mật khẩu và xác nhận mật khẩu không khớp!";
            return;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(emailVal)) {
            errorMessageDiv.textContent = "Email không hợp lệ! Vui lòng nhập đúng định dạng.";
            email.focus();
            return;
        }
        const strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;
        if (!strongPasswordRegex.test(passwordVal)) {
            errorMessageDiv.textContent = "Mật khẩu phải ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!";
            password.focus();
            return;
        }
        registerForm.submit();
    });
});
