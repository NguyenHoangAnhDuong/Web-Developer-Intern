document.addEventListener("DOMContentLoaded", () => {
    function getContextPath() {
        return "/" + window.location.pathname.split("/")[1];
    }
    const apiUrl = getContextPath() + "/admin/users";

    function showToast(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <div class="toast-icon">
                ${type === 'success' ? '✓' : '✕'}
            </div>
            <div class="toast-message">${message}</div>
        `;
        document.body.appendChild(toast);
        setTimeout(() => toast.classList.add('show'), 100);
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    // ===== Filter / Search =====
    const searchInput = document.getElementById("search-input");
    const statusFilter = document.getElementById("status-filter");
    const resetBtn = document.getElementById("reset-filter-btn");

    let debounceTimer;
    function applyFilter() {
        const params = new URLSearchParams();
        const searchTerm = searchInput.value.trim();
        const statusValue = statusFilter.value;
        if (searchTerm) params.append('search', searchTerm);
        if (statusValue) params.append('status', statusValue);
        window.location.href = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
    }

    if (searchInput) {
        searchInput.addEventListener("input", () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(applyFilter, 500);
        });
    }
    if (statusFilter) {
        statusFilter.addEventListener("change", applyFilter);
    }
    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            window.location.href = window.location.pathname;
        });
    }

    // ===== Lock / Unlock =====
    document.querySelectorAll(".toggle-status-icon").forEach(icon => {
        icon.addEventListener("click", async () => {
            const id = icon.dataset.id;
            const newStatus = icon.dataset.status;

            const formData = new URLSearchParams();
            formData.append("id", id);
            formData.append("role", 2);
            formData.append("status", newStatus);

            icon.style.opacity = "0.5";
            icon.style.pointerEvents = "none";

            try {
                const response = await fetch(apiUrl, {
                    method: "POST",
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });
                const result = await response.json();

                icon.style.opacity = "1";
                icon.style.pointerEvents = "auto";

                if (!result.success) {
                    showToast("Cập nhật thất bại", "error");
                    return;
                }

                const row = icon.closest("tr");
                const statusSpan = row.querySelector(".status-text");
                const isActive = newStatus == "1";

                // Cập nhật badge trạng thái
                statusSpan.textContent = isActive ? "Hoạt động" : "Tạm khóa";
                statusSpan.className = "status-text " + (isActive ? "status-active" : "status-locked");

                // Đổi icon lock/unlock
                if (isActive) {
                    icon.className = "fa-solid fa-lock toggle-status-icon lock-icon";
                    icon.dataset.status = "0";
                    icon.title = "Khóa tài khoản";
                } else {
                    icon.className = "fa-solid fa-unlock toggle-status-icon unlock-icon";
                    icon.dataset.status = "1";
                    icon.title = "Mở khóa tài khoản";
                }

                showToast(isActive ? "Mở khóa tài khoản thành công" : "Khóa tài khoản thành công");
            } catch (error) {
                icon.style.opacity = "1";
                icon.style.pointerEvents = "auto";
                showToast("Lỗi kết nối khi cập nhật khách hàng", "error");
            }
        });
    });
});
