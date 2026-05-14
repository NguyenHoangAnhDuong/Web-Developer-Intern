const addressModal = document.getElementById("addressModal");
const addressList = document.getElementById("addressList");
const changeBtn = document.getElementById("changeAddressBtn");
const closeAddressModal = document.getElementById("closeAddressModal");
const checkoutAddressId = document.getElementById("checkoutAddressId");
const checkoutFullName = document.getElementById("checkoutFullName");
const checkoutPhone = document.getElementById("checkoutPhone");
const checkoutFullAddress = document.getElementById("checkoutFullAddress");
const selectedAddressName = document.getElementById("selectedAddressName");
const selectedAddressPhone = document.getElementById("selectedAddressPhone");
const selectedAddressText = document.getElementById("selectedAddressText");
const selectedAddressBadge = document.getElementById("selectedAddressBadge");

function openAddressModal() {
    if (!addressModal) return;
    addressModal.classList.add("open");
    addressModal.setAttribute("aria-hidden", "false");
}

function closeAddressSelection() {
    if (!addressModal) return;
    addressModal.classList.remove("open");
    addressModal.setAttribute("aria-hidden", "true");
}

function setSelectedAddress({ id, name, phone, address, isDefault }) {
    if (checkoutAddressId) checkoutAddressId.value = id || "";
    if (checkoutFullName) checkoutFullName.value = name || "";
    if (checkoutPhone) checkoutPhone.value = phone || "";
    if (checkoutFullAddress) checkoutFullAddress.value = address || "";

    if (selectedAddressName) selectedAddressName.textContent = name || "";
    if (selectedAddressPhone) selectedAddressPhone.textContent = phone ? `(${phone})` : "";
    if (selectedAddressText) selectedAddressText.textContent = address || "";
    if (selectedAddressBadge) selectedAddressBadge.textContent = isDefault ? "Mặc định" : "Đã chọn";

    document.querySelectorAll(".address-option").forEach((option) => {
        option.classList.toggle("active", option.dataset.id === String(id));
    });
}

if (changeBtn && addressModal) {
    changeBtn.addEventListener("click", (e) => {
        e.preventDefault();
        openAddressModal();
    });
}

if (closeAddressModal) {
    closeAddressModal.addEventListener("click", closeAddressSelection);
}

if (addressModal) {
    addressModal.addEventListener("click", (e) => {
        if (e.target === addressModal) {
            closeAddressSelection();
        }
    });
}

if (addressList) {
    addressList.addEventListener("click", (e) => {
        const option = e.target.closest(".address-option");
        if (!option) return;

        setSelectedAddress({
            id: option.dataset.id,
            name: option.dataset.name,
            phone: option.dataset.phone,
            address: option.dataset.address,
            isDefault: option.dataset.default === "true"
        });
        closeAddressSelection();
    });
}
function formatVND(amount) {
    return new Intl.NumberFormat("vi-VN").format(amount) + "₫";
}
function updateFinalTotal() {
    const subtotalEl = document.getElementById("subtotal-val");
    const shippingEl = document.getElementById("shipping-val");
    const discountEl = document.getElementById("discount-display");
    const finalTotalEl = document.getElementById("final-total-display");
    const finalTotalInput = document.getElementById("finalTotalInput");
    const shippingFeeInput = document.getElementById("shippingFeeInput");

    if (!subtotalEl || !shippingEl || !discountEl || !finalTotalEl) return;

    const subtotal = parseFloat(subtotalEl.dataset.value || 0);
    const shipping = parseFloat(shippingEl.dataset.value || 0);
    const discount = parseFloat(
        discountEl.innerText.replace(/\./g, "").replace(/,/g, "") || 0
    );

    const finalTotal = subtotal + shipping - discount;

    finalTotalEl.innerText = formatVND(finalTotal);
    if (finalTotalInput) finalTotalInput.value = finalTotal;
    if (shippingFeeInput) shippingFeeInput.value = shipping;
}

function updateShipping(fee) {
    const shippingEl = document.getElementById("shipping-val");
    const shippingFeeInput = document.getElementById("shippingFeeInput");
    if (!shippingEl) return;

    const shipping = parseFloat(fee || 0);
    shippingEl.dataset.value = shipping;
    shippingEl.innerText = formatVND(shipping);
    if (shippingFeeInput) shippingFeeInput.value = shipping;
    updateFinalTotal();
}

function applyVoucherFromBtn(btn) {
    if (!btn) return;

    const code = btn.dataset.code || "";
    const discountAmount = parseFloat(btn.dataset.discount || 0);
    const minOrder = parseFloat(btn.dataset.minOrder || 0);
    const maxReduce = parseFloat(btn.dataset.maxReduce || 0);
    const type = btn.dataset.type || "";

    applyVoucher(code, discountAmount, minOrder, maxReduce, type, btn);
}

function updateAddress(name, phone, address, id) {
    setSelectedAddress({
        id,
        name,
        phone,
        address,
        isDefault: false
    });
}

const scrollContainer = document.getElementById("voucherScroll");
if (scrollContainer) {
    document.getElementById("nextBtn")?.addEventListener("click", () => {
        scrollContainer.scrollBy({ left: 400, behavior: "smooth" });
    });

    document.getElementById("prevBtn")?.addEventListener("click", () => {
        scrollContainer.scrollBy({ left: -400, behavior: "smooth" });
    });
}

function applyVoucher(code, discountAmount, minOrder, maxReduce, type, btnEl) {
    const subtotalEl = document.getElementById("subtotal-val");
    if (!subtotalEl) return;

    const subtotal = parseFloat(subtotalEl.dataset.value || 0);

    let discount = 0;

    // nếu click vào áp dụng rồi sẽ không được nhấn áp dụng nữa
    const clickedVoucher = btnEl ? btnEl.closest('.voucher') : null;
    if (clickedVoucher && clickedVoucher.classList.contains('active')) {
        // reset discount
        document.getElementById("discount-display").innerText = '0';
        document.getElementById("appliedVoucherInput").value = '';
        document.querySelectorAll(".voucher").forEach(v => v.classList.remove("active"));
        document.querySelectorAll('.voucher-right button').forEach(b => { b.disabled = false; b.innerText = 'Áp dụng'; });
        updateFinalTotal();
        return;
    }

    if (subtotal < minOrder) {
        showToast("Đơn hàng chưa đủ điều kiện áp dụng", "error");
        return;
    }

    if (type === "percentage" || type === "1") {
        discount = subtotal * (discountAmount / 100);

        if (maxReduce > 0 && discount > maxReduce) {
            discount = maxReduce;
        }
    } else {
        discount = discountAmount;
    }

    document.getElementById("discount-display").innerText = discount.toLocaleString("vi-VN");
    document.getElementById("appliedVoucherInput").value = code;
    updateFinalTotal();

   // voucher áp dụng và cập nhập button
    document.querySelectorAll(".voucher").forEach(v => v.classList.remove("active"));
    document.querySelectorAll('.voucher-right button').forEach(b => { b.disabled = false; b.innerText = 'Áp dụng'; });

    if (btnEl) {
        const parent = btnEl.closest('.voucher');
        parent?.classList.add('active');
        btnEl.innerText = 'Đã áp dụng';
        btnEl.disabled = true;
    }
}
document.addEventListener("DOMContentLoaded", function () {
    const orderBtn = document.querySelector(".round-black-btn");
    const orderForm = document.querySelector("form[action='placeOrder']");
    const toggleShippingBtn = document.getElementById("toggleShippingOptions");
    const hiddenShippingOptions = document.querySelectorAll(".shipping-option-hidden");
    if (orderBtn && orderForm) {
        orderBtn.addEventListener("click", function (e) {
            e.preventDefault(); // Ngăn chặn mọi hành động mặc định
            // Kiểm tra Address ID trước khi gửi
            const addressInput = document.querySelector("input[name='addressId']");
            const addressId = addressInput ? addressInput.value : "";
                orderForm.submit();

        });
    }

    document.querySelectorAll("input.shipping-option").forEach((radio) => {
        radio.addEventListener("change", function () {
            updateShipping(this.dataset.fee);
        });
    });

    const checkedShipping = document.querySelector("input.shipping-option:checked");
    if (checkedShipping) {
        updateShipping(checkedShipping.dataset.fee);
    } else {
        updateFinalTotal();
    }

    if (toggleShippingBtn) {
        toggleShippingBtn.addEventListener("click", function () {
            const isExpanded = this.dataset.expanded === "true";
            hiddenShippingOptions.forEach((item) => {
                item.style.display = isExpanded ? "none" : "block";
            });
            this.dataset.expanded = isExpanded ? "false" : "true";
            this.innerText = isExpanded ? "Xem thêm" : "Ẩn bớt";
        });
    }

    const appliedCode = document.getElementById("appliedVoucherInput")?.value || "";
    if (appliedCode) {
        const appliedButton = Array.from(document.querySelectorAll(".voucher-right button"))
            .find((button) => (button.dataset.code || "") === appliedCode);

        if (appliedButton) {
            const code = appliedButton.dataset.code || "";
            const discountAmount = parseFloat(appliedButton.dataset.discount || 0);
            const minOrder = parseFloat(appliedButton.dataset.minOrder || 0);
            const maxReduce = parseFloat(appliedButton.dataset.maxReduce || 0);
            const type = appliedButton.dataset.type || "";
            applyVoucher(code, discountAmount, minOrder, maxReduce, type, appliedButton);
        }
    }

    const activeAddressOption = document.querySelector(".address-option.active");
    if (activeAddressOption && checkoutAddressId) {
        setSelectedAddress({
            id: activeAddressOption.dataset.id,
            name: activeAddressOption.dataset.name,
            phone: activeAddressOption.dataset.phone,
            address: activeAddressOption.dataset.address,
            isDefault: activeAddressOption.dataset.default === "true"
        });
    }
});
