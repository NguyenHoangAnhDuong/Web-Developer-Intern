document.addEventListener("DOMContentLoaded", () => {
    const editableInputs = document.querySelectorAll(".editable-input");
    const updateBtn = document.getElementById("update-btn");
    const saveBtn = document.getElementById("save-btn");

    if (updateBtn) {
        updateBtn.addEventListener("click", () => {
            // Bật các input cho phép chỉnh sửa
            editableInputs.forEach(input => {
                input.disabled = false;
                input.classList.add("editing");
            });
            // Đổi nút Cập nhật → Lưu
            updateBtn.style.display = "none";
            saveBtn.style.display = "inline-block";
            // Focus vào field đầu tiên
            if (editableInputs.length > 0) editableInputs[0].focus();
        });
    }

    // ====== Đặt lại mật khẩu (admin/nhân viên hỗ trợ) ======
    const resetBtn = document.getElementById("reset-pwd-btn");
    const resetForm = document.getElementById("reset-pwd-form");
    const cancelBtn = document.getElementById("cancel-pwd-btn");
    const newPwdInput = document.getElementById("newPassword");
    const togglePwdBtn = document.getElementById("toggle-pwd-btn");

    if (resetBtn && resetForm) {
        resetBtn.addEventListener("click", () => {
            resetForm.style.display = "block";
            resetBtn.style.display = "none";
            if (newPwdInput) newPwdInput.focus();
        });
    }

    if (cancelBtn && resetForm && resetBtn) {
        cancelBtn.addEventListener("click", () => {
            resetForm.style.display = "none";
            resetBtn.style.display = "inline-flex";
            if (newPwdInput) newPwdInput.value = "";
        });
    }

    if (togglePwdBtn && newPwdInput) {
        togglePwdBtn.addEventListener("click", () => {
            const isHidden = newPwdInput.type === "password";
            newPwdInput.type = isHidden ? "text" : "password";
            togglePwdBtn.innerHTML = isHidden
                ? '<i class="fa-solid fa-eye-slash"></i>'
                : '<i class="fa-solid fa-eye"></i>';
        });
    }

    if (resetForm) {
        resetForm.addEventListener("submit", (e) => {
            const value = (newPwdInput?.value || "").trim();
            if (!value) {
                e.preventDefault();
                alert("Vui lòng nhập mật khẩu mới.");
                return;
            }
            if (!confirm("Xác nhận cập nhật mật khẩu mới cho khách hàng này?")) {
                e.preventDefault();
            }
        });
    }
});
