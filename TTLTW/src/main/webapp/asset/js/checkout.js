const addressList = document.getElementById("addressList");
const changeBtn = document.getElementById("changeAddressBtn");

if (changeBtn && addressList) {
    changeBtn.addEventListener("click", (e) => {
        e.preventDefault();
        addressList.classList.toggle("hidden");
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
    const nameEl = document.querySelector(".address strong");
    const phoneEl = document.querySelector(".address span");
    const addrEl = document.querySelector(".address p:nth-of-type(2)");
    const hiddenInput = document.querySelector("input[name='addressId']");

    if (nameEl) nameEl.textContent = name;
    if (phoneEl) phoneEl.textContent = `(${phone})`;
    if (addrEl) addrEl.childNodes[0].textContent = address + ' ';
    if (hiddenInput) hiddenInput.value = id;

    if (addressList) addressList.classList.add("hidden");
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

    document.getElementById("discount-display").innerText =
        discount.toLocaleString("vi-VN");

    document.getElementById("appliedVoucherInput").value = code;

    updateFinalTotal();

    document.querySelectorAll(".voucher")
        .forEach(v => v.classList.remove("active"));

    const btn = btnEl;
    if (btn) {
        btn.closest(".voucher")?.classList.add("active");
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
});