
document.addEventListener("DOMContentLoaded", function () {
    const selectAll = document.getElementById("selectAll");
    const itemCheckboxes = document.querySelectorAll(".select-item");
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
            document.querySelectorAll(".select-item").forEach(cb => cb.checked = selectAll.checked);
            updateTotalPrice();
        });
    }

    document.querySelectorAll(".select-item").forEach(checkbox => {
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
                if (checkbox.checked) {
                    updateTotalPrice();
                }
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
// hàm cập nhật tổng tiền và trạng thái nút thanh toán
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
// hàm xử lý khi đổi biến thể
function onVariantLevel1Change(select) {
    const row = select.closest("tr");
    const checkbox = row.querySelector(".select-item");

    const oldVcId = checkbox.dataset.id;
    const variantId = select.value;

    fetch(`api/colors-by-variant?variantId=${variantId}`)
        .then(res => res.json())
        .then(colors => {

            const colorSelect = row.querySelector(".color-select");
            colorSelect.innerHTML = "";

            colors.forEach(c => {
                const opt = document.createElement("option");
                opt.value = c.id;
                opt.textContent = c.color_name;
                colorSelect.appendChild(opt);
            });

            const newVcId = colors[0].id;
            changeVariant(oldVcId, newVcId);
        });
}
// hàm xử lý khi đổi màu
function onColorChange(select) {
    const row = select.closest("tr");
    const checkbox = row.querySelector(".select-item");

    const oldVcId = checkbox.dataset.id;
    const newVcId = select.value;

    changeVariant(oldVcId, newVcId);
}
// hàm thay đổi biến thể , gọi API để cập nhật và sau đó cập nhật lại giao diện
function changeVariant(oldId, newId) {
    fetch(`cart?action=changeVariant&oldVcId=${oldId}&newVcId=${newId}`)
        .then(res => res.json())
        .then(data => {
            if (data.status === "success") {

                showToast("Đã cập nhật", "success");

                updateRowAfterChange(oldId, newId);

            } else {
                showToast(data.message, "error");
            }
        });
}
// hàm cập nhật lại giao diện sau khi đổi biến thể thành công
function updateRowAfterChange(oldId, newId) {
    const checkbox = document.querySelector(`.select-item[data-id="${oldId}"]`);
    if (!checkbox) return;

    const row = checkbox.closest("tr");

    // cập nhật id mới
    checkbox.dataset.id = newId;

    // update nút
    row.querySelector(".minus").setAttribute("onclick", `updateQty(${newId}, -1)`);
    row.querySelector(".plus").setAttribute("onclick", `updateQty(${newId}, 1)`);
    row.querySelector(".delete").setAttribute("onclick", `removeItem(${newId})`);

    // lấy giá mới
    fetch(`api/variant-info?id=${newId}`)
        .then(res => res.json())
        .then(data => {

            const qty = parseInt(row.querySelector(".quantity").innerText);
            const newSubtotal = data.price * qty;

            checkbox.dataset.price = newSubtotal;
            checkbox.dataset.unitPrice = data.price;

            row.querySelector(".price").innerText =
                new Intl.NumberFormat('vi-VN').format(newSubtotal) + "₫";

            if (checkbox.checked) {
                updateTotalPrice();
            }
        });
}