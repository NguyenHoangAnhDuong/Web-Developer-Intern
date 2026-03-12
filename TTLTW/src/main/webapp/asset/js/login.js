document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("login-form");
    const usernameInput = document.getElementById("login-username");
    const passwordInput = document.getElementById("login-password");
    const messageDiv = document.getElementById("login-message");

    function showMessage(msg) {
        messageDiv.textContent = msg;
        messageDiv.classList.add("show");
    }

    function clearError() {
        usernameInput.classList.remove("error");
        passwordInput.classList.remove("error");
        messageDiv.textContent = "";
    }

    form.addEventListener("submit", function (e) {
        clearError();

        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        if (!username) {
            e.preventDefault();
            showMessage("Vui lòng nhập tên đăng nhập hoặc email!");
            usernameInput.classList.add("error");
            return;
        }

        if (!password) {
            e.preventDefault();
            showMessage("Vui lòng nhập mật khẩu!");
            passwordInput.classList.add("error");
            return;
        }

    });
});
