document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("modalOverlay");
    const form = document.getElementById("addressForm");
    const addressList = document.getElementById("addressList");
    const btnAdd = document.getElementById("btnAddAddress");
    const btnBack = document.getElementById("btnBack");

    const inputName = document.getElementById("name");
    const inputPhone = document.getElementById("phoneNumber");
    const inputAddress = document.getElementById("detailAddress");
    const inputStatus = document.getElementById("status");

    let editingId = null;
    // địa chỉ
    const provinceInput = document.getElementById("provinceInput");
    const districtInput = document.getElementById("districtInput");
    const wardInput = document.getElementById("wardInput");

    const provinceList = document.getElementById("provinceList");
    const districtList = document.getElementById("districtList");
    const wardList = document.getElementById("wardList");
    let provinces = [];
    let districts = [];
    let wards = [];
    let selectedProvince = null;
    let selectedDistrict = null;
    let selectedWard = null;
    // Đầu số hợp lệ các nhà mạng Việt Nam
    const VN_PHONE_PREFIXES = [
        // Viettel
        "032","033","034","035","036","037","038","039",
        "086","096","097","098",
        // Mobifone
        "070","076","077","078","079",
        "089","090","093",
        // Vinaphone
        "081","082","083","084","085",
        "088","091","094",
        // Vietnamobile
        "052","056","058","092",
        // Gmobile
        "059","099",
        // Reddi
        "055"
    ];
    function validateName(name) {
        if (!name || name.length < 2)
            return { ok: false, msg: "Họ tên phải có ít nhất 2 ký tự." };
        if (name.length > 100)
            return { ok: false, msg: "Họ tên không được vượt quá 100 ký tự." };
        return { ok: true };
    }
    function validatePhone(phone) {
        if (!/^[0-9]{10}$/.test(phone))
            return { ok: false, msg: "Số điện thoại phải có đúng 10 chữ số." };
        const prefix = phone.substring(0, 3);
        if (!VN_PHONE_PREFIXES.includes(prefix))
            return { ok: false, msg: `Đầu số "${prefix}" không hợp lệ. Vui lòng nhập số điện thoại Việt Nam.` };
        return { ok: true };
    }
    function validateAddress(addr) {
        if (!addr || addr.length < 3)
            return { ok: false, msg: "Địa chỉ chi tiết phải có ít nhất 3 ký tự." };
        if (addr.length > 255)
            return { ok: false, msg: "Địa chỉ chi tiết không được vượt quá 255 ký tự." };
        return { ok: true };
    }
    // Hiển thị / xóa lỗi inline dưới input
    function setFieldError(inputEl, msg) {
        clearFieldError(inputEl);
        inputEl.classList.add("input-error");
        const err = document.createElement("span");
        err.className = "field-error";
        err.innerHTML = `<i class="fa-solid fa-circle-exclamation"></i> ${msg}`;
        inputEl.parentNode.appendChild(err);
    }
    function clearFieldError(inputEl) {
        inputEl.classList.remove("input-error");
        const existing = inputEl.parentNode.querySelector(".field-error");
        if (existing) existing.remove();
    }
    function clearAllErrors() {
        form.querySelectorAll(".field-error").forEach(el => el.remove());
        form.querySelectorAll(".input-error").forEach(el => el.classList.remove("input-error"));
    }
    function resetLocationFields() {
        provinceInput.value = "";
        districtInput.value = "";
        wardInput.value = "";
        provinceList.innerHTML = "";
        districtList.innerHTML = "";
        wardList.innerHTML = "";
        selectedProvince = null;
        selectedDistrict = null;
        selectedWard = null;
        districts = [];
        wards = [];

        districtInput.disabled = true;
        wardInput.disabled = true;
    }
    [inputName, inputPhone, inputAddress].forEach(el => {
        el.addEventListener("input", () => clearFieldError(el));
    });
    // Validate khi blur
    inputName.addEventListener("blur", () => {
        const r = validateName(inputName.value.trim());
        if (!r.ok) setFieldError(inputName, r.msg); else clearFieldError(inputName);
    });
    inputPhone.addEventListener("blur", () => {
        const r = validatePhone(inputPhone.value.trim());
        if (!r.ok) setFieldError(inputPhone, r.msg); else clearFieldError(inputPhone);
    });
    inputAddress.addEventListener("blur", () => {
        const r = validateAddress(inputAddress.value.trim());
        if (!r.ok) setFieldError(inputAddress, r.msg); else clearFieldError(inputAddress);
    });
    function openModal(isEdit = false) {
        document.getElementById("modalTitleText").textContent = isEdit ? "Cập nhật địa chỉ" : "Địa chỉ mới";
        modal.classList.add("active");
        if (!isEdit) {
            resetLocationFields();
        }
        loadProvinces();
    }
    function closeModal() {
        modal.classList.remove("active");
        form.reset();
        editingId = null;
       resetLocationFields();
    }

    btnAdd?.addEventListener("click",() => openModal(false));
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

        const fullAddr = item.querySelector(".address-details")?.innerText.trim() ?? "";
        // Tách phần địa chỉ chi tiết (trước dấu phẩy đầu tiên)
        const parts = fullAddr.split(",");
        inputAddress.value = parts[0]?.trim() ?? fullAddr;

        // Điền lại tỉnh/huyện/xã từ text đã lưu (chỉ hiển thị, không cần gọi API lại)
        if (parts.length >= 4) {
            wardInput.value = parts[1]?.trim() ?? "";
            districtInput.value = parts[2]?.trim() ?? "";
            provinceInput.value = parts[3]?.trim() ?? "";

            // Gán giả để pass validation khi update
            selectedProvince = { name: provinceInput.value };
            selectedDistrict = { name: districtInput.value };
            selectedWard = { name: wardInput.value };
        }
    }

    form?.addEventListener("submit", async e => {
        e.preventDefault();

        if (!validatePhone(inputPhone.value.trim())) {
            showToast('Số điện thoại phải có 10 chữ số', 'error');
            return;
        }
        if (!selectedProvince || !selectedDistrict || !selectedWard) {
            showToast("Vui lòng chọn đầy đủ Tỉnh / Huyện / Xã", "error");
            return;
        }
        const fullAddress = `${inputAddress.value}, ${selectedWard?.name}, ${selectedDistrict?.name}, ${selectedProvince?.name}`;
        const payload = {
            name: inputName.value.trim(),
            phoneNumber: inputPhone.value.trim(),
            fullAddress: fullAddress,
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
    async function loadProvinces() {
        if (provinces.length > 0) return; // cache, không gọi lại
        try {
            const res = await fetch("https://provinces.open-api.vn/api/p/");
            provinces = await res.json();
        } catch {
            showToast("Không thể tải danh sách tỉnh/thành", "error");
        }
    }
    provinceInput.addEventListener("input", () => {
        const keyword = provinceInput.value.toLowerCase().trim();
        if (!keyword) { provinceList.innerHTML = ""; return; }
        const filtered = provinces.filter(p => p.name.toLowerCase().includes(keyword));
        renderList(filtered, provinceList, (item) => {
            provinceInput.value = item.name;
            selectedProvince = item;
            provinceList.innerHTML = "";
            // Reset huyện/xã khi đổi tỉnh
            districtInput.value = "";
            wardInput.value = "";
            districtList.innerHTML = "";
            wardList.innerHTML = "";
            selectedDistrict = null;
            selectedWard = null;
            districts = [];
            wards = [];
            districtInput.disabled = false; // Mở khoá huyện
            loadDistricts(item.code);
        });
    });
    async function loadDistricts(provinceCode) {
        try {
            const res = await fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
            const data = await res.json();
            districts = data.districts ?? [];
        } catch {
            showToast("Không thể tải danh sách huyện", "error");
        }
    }
    districtInput.addEventListener("input", () => {
        const keyword = districtInput.value.toLowerCase().trim();
        if (!keyword) { districtList.innerHTML = ""; return; }
        const filtered = districts.filter(d => d.name.toLowerCase().includes(keyword));
        renderList(filtered, districtList, (item) => {
            districtInput.value = item.name;
            selectedDistrict = item;
            districtList.innerHTML = "";
            // Reset xã khi đổi huyện
            wardInput.value = "";
            wardList.innerHTML = "";
            selectedWard = null;
            wards = [];
            wardInput.disabled = false; //  Mở khoá xã
            loadWards(item.code);
        });
    });
    async function loadWards(districtCode) {
        try {
            const res = await fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
            const data = await res.json();
            wards = data.wards ?? [];
        } catch {
            showToast("Không thể tải danh sách xã", "error");
        }
    }
    wardInput.addEventListener("input", () => {
        const keyword = wardInput.value.toLowerCase().trim();
        if (!keyword) { wardList.innerHTML = ""; return; }
        const filtered = wards.filter(w => w.name.toLowerCase().includes(keyword));
        renderList(filtered, wardList, (item) => {
            wardInput.value = item.name;
            selectedWard = item;
            wardList.innerHTML = "";
        });
    });
    function renderList(data, container, onClick) {
        container.innerHTML = "";
        data.slice(0, 10).forEach(item => {
            const div = document.createElement("div");
            div.className = "suggest-item";
            div.textContent = item.name;
            div.addEventListener("mousedown", (e) => {
                e.preventDefault(); //Tránh blur input trước khi click được xử lý
                onClick(item);
            });
            container.appendChild(div);
        });
    }
    document.addEventListener("click", (e) => {
        if (!provinceInput.contains(e.target)) provinceList.innerHTML = "";
        if (!districtInput.contains(e.target)) districtList.innerHTML = "";
        if (!wardInput.contains(e.target)) wardList.innerHTML = "";
    });
    districtInput.disabled = true;
    wardInput.disabled = true;
});