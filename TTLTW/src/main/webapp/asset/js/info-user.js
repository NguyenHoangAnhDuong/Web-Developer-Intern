document.addEventListener("DOMContentLoaded", function () {
    const context = window.contextPath || "";

    const btnEdit = document.getElementById("btn-edit");
    const btnSave = document.getElementById("btn-save");
    const btnLogout = document.getElementById("btn-logout");

    const firstNameInput = document.getElementById("firstName");
    const lastNameInput = document.getElementById("lastName");
    const emailInput = document.getElementById("emailInput");

    const avatarInput = document.getElementById("avatarInput");
    const avatarPreview = document.getElementById("user-avatar");
    const sidebarAvatar = document.getElementById("userAvatar");
    const updateForm = document.getElementById("update-form");

    if (btnEdit) {
        btnEdit.addEventListener("click", function () {
            firstNameInput.disabled = false;
            lastNameInput.disabled = false;
            btnEdit.classList.add("hidden");
            btnLogout.classList.add("hidden");
            btnSave.classList.remove("hidden");
            showToast('Bạn có thể chỉnh sửa thông tin', 'info');
        });
    }
    if (btnLogout) {
        btnLogout.addEventListener("click", async function (e) {
            e.preventDefault();
            const confirmed = await confirmLogout();
            if (confirmed) {
                showToast('Đang đăng xuất...', 'info');
                setTimeout(() => {
                    window.location.href = context + "/logout";
                }, 800);
            }
        });
    }
    if (updateForm) {
        updateForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            if (emailInput.value.trim() && !validateEmail(emailInput.value.trim())) {
                showToast('Email không hợp lệ', 'error');
                return;
            }
            if (!firstNameInput.value.trim()) {
                showToast('Vui lòng nhập họ', 'error');
                return;
            }
            if (!lastNameInput.value.trim()) {
                showToast('Vui lòng nhập tên', 'error');
                return;
            }
            firstNameInput.disabled = false;
            lastNameInput.disabled = false;
            emailInput.disabled = false;

            const fd = new FormData(updateForm);
            fd.append("action", "update");

            btnSave.disabled = true;
            const originalText = btnSave.textContent;
            btnSave.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';

            try {
                const res = await fetch(context + "/user/profile", {
                    method: "POST",
                    body: fd
                });

                if (!res.ok) throw new Error("HTTP " + res.status);

                const result = await res.json();

                if (result.success) {
                    showToast("Cập nhật thông tin thành công!", "success");

                    setTimeout(() => {
                        firstNameInput.disabled = true;
                        lastNameInput.disabled = true;
                        emailInput.disabled = true;

                        btnEdit.classList.remove("hidden");
                        btnLogout.classList.remove("hidden");
                        btnSave.classList.add("hidden");

                        btnSave.disabled = false;
                        btnSave.textContent = originalText;
                    }, 1000);
                } else {
                    showToast(result.message || "Cập nhật thất bại!", "error");
                    btnSave.disabled = false;
                    btnSave.innerHTML = originalText;
                }

            } catch (error) {
                console.error("Lỗi khi cập nhật:", error);
                showToast("Có lỗi xảy ra. Vui lòng thử lại.", "error");
                btnSave.disabled = false;
                btnSave.innerHTML = originalText;
            }
        });
    }
    if (avatarInput) {
        avatarInput.addEventListener("change", async function () {
            const file = avatarInput.files[0];
            if (!file) return;
            // Kiểm tra kích thước
            if (file.size > 5 * 1024 * 1024) {
                showToast("Kích thước ảnh không được vượt quá 5MB", "error");
                avatarInput.value = "";
                return;
            }
            // Kiểm tra định dạng
            if (!file.type.match(/image\/(png|jpeg|jpg|gif)/)) {
                showToast("Chỉ chấp nhận file ảnh (PNG, JPG, JPEG, GIF)", "error");
                avatarInput.value = "";
                return;
            }
            const fd = new FormData();
            fd.append("action", "upload-avatar");
            fd.append("avatar", file);
            const reader = new FileReader();
            reader.onload = (e) => avatarPreview.src = e.target.result;
            reader.readAsDataURL(file);
            const loadingOverlay = document.createElement('div');
            loadingOverlay.className = 'avatar-loading-overlay';
            loadingOverlay.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';
            avatarPreview.parentElement.appendChild(loadingOverlay);
            try {
                const res = await fetch(context + "/user/profile", {
                    method: "POST",
                    body: fd
                });
                if (!res.ok) throw new Error("HTTP " + res.status);
                const result = await res.json();
                if (result.success) {
                    const newAvatarUrl = result.url + "?t=" + Date.now();
                    if (avatarPreview) avatarPreview.src = newAvatarUrl;
                    if (sidebarAvatar) sidebarAvatar.src = newAvatarUrl;
                    showToast("Upload ảnh đại diện thành công!", "success");
                } else {
                    showToast(result.message || "Upload thất bại!", "error");
                }
            } catch (error) {
                console.error("Lỗi khi upload avatar:", error);
                showToast("Có lỗi xảy ra. Vui lòng thử lại.", "error");
            } finally {
                if (loadingOverlay && loadingOverlay.parentElement) {
                    loadingOverlay.remove();
                }
            }
            avatarInput.value = "";
        });
    }
    if (emailInput) {
        emailInput.addEventListener('blur', function () {
            const emailError = document.getElementById('emailError');
            if (this.value.trim() && !validateEmail(this.value.trim())) {
                emailError.textContent = 'Email không hợp lệ';
                this.style.borderColor = '#ef4444';
            } else {
                emailError.textContent = '';
                this.style.borderColor = '';
            }
        });
        emailInput.addEventListener('input', function () {
            if (this.style.borderColor === 'rgb(239, 68, 68)') {
                const emailError = document.getElementById('emailError');
                if (validateEmail(this.value.trim())) {
                    emailError.textContent = '';
                    this.style.borderColor = '';
                }
            }
        });
        if (menuAccountMain && accountSubmenu) {
            accountSubmenu.classList.add("open");
            menuAccountMain.addEventListener("click", (e) => {
                e.preventDefault();
                accountSubmenu.classList.toggle("open");
            });
        }
    }
});