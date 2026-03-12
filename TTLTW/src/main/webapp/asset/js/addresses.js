document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("modalOverlay");
    const form = document.getElementById("addressForm");
    const addressList = document.getElementById("addressList");
    const btnAdd = document.getElementById("btnAddAddress");
    const btnBack = document.getElementById("btnBack");

    const inputName = document.getElementById("name");
    const inputPhone = document.getElementById("phoneNumber");
    const inputAddress = document.getElementById("fullAddress");
    const inputStatus = document.getElementById("status");

    let editingId = null;

    function openModal() {
        modal.classList.add("active");
    }

    function closeModal() {
        modal.classList.remove("active");
        form.reset();
        editingId = null;
    }

    btnAdd?.addEventListener("click", openModal);
    btnBack?.addEventListener("click", closeModal);
    modal?.addEventListener("click", e => {
        if (e.target === modal) closeModal();
    });

    async function post(data) {
        const res = await fetch(`${window.contextPath}/user/addresses`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams(data)
        });
        return res.json();
    }

    function fillForm(item) {
        inputName.value = item.querySelector(".address-name").innerText.trim();
        inputPhone.value = item.querySelector(".address-phone").innerText.trim();
        inputAddress.value = item.querySelector(".address-details").innerText.trim();
        inputStatus.checked = item.classList.contains("default");
    }

    form?.addEventListener("submit", async e => {
        e.preventDefault();

        if (!validatePhone(inputPhone.value.trim())) {
            showToast('Số điện thoại phải có 10 chữ số', 'error');
            return;
        }

        const payload = {
            name: inputName.value.trim(),
            phoneNumber: inputPhone.value.trim(),
            fullAddress: inputAddress.value.trim(),
            status: inputStatus.checked ? 1 : 0
        };

        let actionText = "";
        if (editingId) {
            payload.action = "update";
            payload.id = editingId;
            actionText = "Cập nhật địa chỉ thành công!";
        } else {
            payload.action = "add";
            actionText = "Thêm địa chỉ mới thành công!";
        }

        try {
            const res = await post(payload);

            if (res.success) {
                closeModal();
                showToast(actionText, 'success');
                setTimeout(() => location.reload(), 1000);
            } else {
                showToast(res.message || "Thao tác thất bại", 'error');
            }
        } catch (error) {
            showToast("Có lỗi xảy ra, vui lòng thử lại", 'error');
        }
    });

    addressList?.addEventListener("click", async e => {
        const btn = e.target.closest("[data-action]");
        if (!btn) return;

        e.preventDefault();
        const id = btn.dataset.id;
        const action = btn.dataset.action;
        const item = document.querySelector(`.address-item[data-id="${id}"]`);

        if (action === "update") {
            editingId = id;
            fillForm(item);
            openModal();
        }

        if (action === "delete") {
            const addressName = item.querySelector(".address-name").innerText.trim();

            const confirmed = await confirmDelete(`địa chỉ của <strong>${addressName}</strong>`);
            if (!confirmed) return;

            try {
                const res = await post({ action: "delete", id });

                if (res.success) {
                    item.style.transition = 'all 0.3s ease';
                    item.style.opacity = '0';
                    item.style.transform = 'translateX(-20px)';

                    setTimeout(() => {
                        item.remove();
                        showToast("Xóa địa chỉ thành công!", 'success');

                        const remainingAddresses = addressList.querySelectorAll('.address-item');
                        if (remainingAddresses.length === 0) {
                            addressList.innerHTML = `
                                <div class="address-empty">
                                    <p>Bạn chưa có địa chỉ nào. Hãy thêm địa chỉ mới.</p>
                                </div>
                            `;
                        }
                    }, 300);
                } else {
                    showToast(res.message || "Xóa thất bại", 'error');
                }
            } catch (error) {
                showToast("Có lỗi xảy ra khi xóa địa chỉ", 'error');
            }
        }

        if (action === "set-default") {
            try {
                const res = await post({ action: "set-default", id });

                if (res.success) {
                    showToast("Đã đặt làm địa chỉ mặc định!", 'success');
                    setTimeout(() => location.reload(), 1000);
                } else {
                    showToast(res.message || "Không thể đặt mặc định", 'error');
                }
            } catch (error) {
                showToast("Có lỗi xảy ra, vui lòng thử lại", 'error');
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
});