
document.addEventListener("DOMContentLoaded", function () {
    const selectAll = document.getElementById("selectAll");
    const itemCheckboxes = document.querySelectorAll(".select-item");
    const subTotalDisplay = document.getElementById("sub-total");
    const checkoutBtn = document.querySelector(".checkout-btn");

    function formatVND(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount) + "₫";
    }
    if (checkoutBtn) {
        checkoutBtn.addEventListener("click", function(e) {
            e.preventDefault();
            e.stopPropagation(); // Thêm dòng này để ngăn các sự kiện lồng nhau

            const selectedCheckboxes = document.querySelectorAll(".select-item:checked");

            if (selectedCheckboxes.length === 0) {
                showToast("Vui lòng chọn ít nhất một sản phẩm!", "error");
                return;
            }
            const selectedIds = Array.from(selectedCheckboxes).map(cb => cb.getAttribute("data-id"));
            // Chuyển hướng
            window.location.href = "cart?action=checkout&selectedIds=" + selectedIds.join(",");
        });
    }

    if (selectAll) {
        selectAll.addEventListener("change", function () {
            itemCheckboxes.forEach(cb => cb.checked = selectAll.checked);
            updateTotalPrice();
        });
    }

    itemCheckboxes.forEach(checkbox => {
        checkbox.addEventListener("change", function () {
            // Nếu có 1 ô bị bỏ tích, ô "Chọn tất cả" phải bỏ tích theo
            if (!this.checked) {
                if(selectAll) selectAll.checked = false;
            }
            // Nếu tất cả ô con được tích, tự động tích ô "Chọn tất cả"
            else {
                const allChecked = document.querySelectorAll(".select-item:checked").length === itemCheckboxes.length;
                if(selectAll) selectAll.checked = allChecked;
            }
            updateTotalPrice();
        });
    });
    updateTotalPrice();
});
function updateQty(id, delta) {
    const checkbox = document.querySelector(`.select-item[data-id="${id}"]`);
    if (!checkbox) return;

    const row = checkbox.closest("tr");
    const qtyElement = row.querySelector('.quantity');
    const priceElement = row.querySelector('.price');

    fetch(`cart?action=update&vcId=${id}&delta=${delta}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === "success") {
                let currentQty = parseInt(qtyElement.innerText);
                let newQty = currentQty + delta;

                if (newQty <= 0) {
                    row.remove();
                } else {
                    qtyElement.innerText = newQty;

                    const unitPrice = parseFloat(checkbox.dataset.unitPrice);
                    const newSubtotal = unitPrice * newQty;
                    checkbox.dataset.price = newSubtotal;
                    priceElement.innerText =
                        new Intl.NumberFormat('vi-VN').format(newSubtotal) + "₫";
                }
                updateTotalPrice();
            } else {

                showToast("Sản phẩm đã hết hàng!", "error");
            }
        })
        .catch(err => console.error("Lỗi kết nối:", err));
}
function removeItem(id) {
    fetch(`cart?action=remove&vcId=${id}`)
        .then(() => {
            const checkbox = document.querySelector(`.select-item[data-id="${id}"]`);
            if (!checkbox) return;

            const row = checkbox.closest("tr");
            row.remove();

            updateTotalPrice();

            showToast("Đã xóa sản phẩm", "success");
        })
        .catch(() => {
            showToast("Xóa thất bại!", "error");
        });
}
function updateTotalPrice() {
    let total = 0;
    let hasChecked = false;
    const checkedItems = document.querySelectorAll(".select-item:checked");
    checkedItems.forEach(cb => {
        total += parseFloat(cb.dataset.price || 0);
        hasChecked = true;
    });
    const subTotalDisplay = document.getElementById("sub-total");
    const checkoutBtn = document.querySelector(".checkout-btn");

    if (subTotalDisplay) {
        subTotalDisplay.innerText =
            new Intl.NumberFormat('vi-VN').format(total) + "₫";
    }
    if (checkoutBtn) {
        checkoutBtn.disabled = !hasChecked;
        checkoutBtn.style.opacity = hasChecked ? "1" : "0.5";
    }
}