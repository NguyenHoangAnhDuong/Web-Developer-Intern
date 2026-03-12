const addressList = document.getElementById("addressList");
const changeBtn = document.getElementById("changeAddressBtn");

if (changeBtn && addressList) {
    changeBtn.addEventListener("click", (e) => {
        e.preventDefault();
        addressList.classList.toggle("hidden");
    });
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

function applyVoucher(code, discountAmount, minOrder, maxReduce, type) {
    try {
        const subtotalEl = document.getElementById('subtotal-val');
        const shippingEl = document.getElementById('shipping-val');

        if (!subtotalEl || !shippingEl) {
            console.error("Không tìm thấy phần tử hiển thị giá tiền.");
            return;
        }

        const subtotal = parseFloat(subtotalEl.getAttribute('data-value'));
        const shipping = parseFloat(shippingEl.getAttribute('data-value'));

        let discount = 0;
        if (type === 'percentage' || type === '1') {
            discount = subtotal * (discountAmount / 100);
            if (maxReduce > 0 && discount > maxReduce) {
                discount = maxReduce;
            }
        } else {
            discount = discountAmount;
        }

        const discountDisplay = document.getElementById('discount-display');
        const finalTotalDisplay = document.getElementById('final-total-display');

        if (discountDisplay) {
            discountDisplay.innerText = discount.toLocaleString("vi-VN");
        }

        if (finalTotalDisplay) {
            const finalTotal = subtotal + shipping - discount;
            finalTotalDisplay.innerText = finalTotal.toLocaleString("vi-VN") + "₫";
        }

        const voucherInput = document.getElementById('appliedVoucherInput');
        if (voucherInput) {
            voucherInput.value = code;
        }

        document.querySelectorAll('.voucher').forEach(v => v.classList.remove('active'));
        // Tìm element cha để highlight (sửa lỗi 'event is not defined' trong một số trình duyệt)
        const btn = window.event ? window.event.target : null;
        if (btn) {
            btn.closest('.voucher').classList.add('active');
        }
    } catch (error) {
        console.error("Lỗi khi áp dụng voucher:", error);
    }
}
document.addEventListener("DOMContentLoaded", function () {
    const orderBtn = document.querySelector(".round-black-btn");
    const orderForm = document.querySelector("form[action='placeOrder']");
    if (orderBtn && orderForm) {
        orderBtn.addEventListener("click", function (e) {
            e.preventDefault(); // Ngăn chặn mọi hành động mặc định
            // Kiểm tra Address ID trước khi gửi
            const addressInput = document.querySelector("input[name='addressId']");
            const addressId = addressInput ? addressInput.value : "";
                orderForm.submit();

        });
    }
});