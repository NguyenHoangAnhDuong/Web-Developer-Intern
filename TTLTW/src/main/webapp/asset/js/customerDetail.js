document.addEventListener("DOMContentLoaded", () => {
    const editableInputs = document.querySelectorAll(".editable-input");
    const updateBtn = document.getElementById("update-btn");
    const saveBtn = document.getElementById("save-btn");

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
});
