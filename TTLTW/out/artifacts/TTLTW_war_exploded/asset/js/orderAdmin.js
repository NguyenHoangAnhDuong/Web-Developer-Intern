const searchInput = document.getElementById('searchInput');
const statusFilterSelect = document.getElementById('statusFilter');
const tableBody = document.querySelector('#ordersTable tbody');
let currentOrderIdForCancel = null;
function bindStatusChange() {
    document.querySelectorAll('.status-select').forEach(select => {
        updateStatusColor(select);
        select.addEventListener('change', function () {
            const orderId = this.dataset.id;
            const status = this.value;
            updateStatusColor(this);
            fetch(`${contextPath}/admin/orders`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `orderId=${orderId}&status=${status}`
            })
                .then(res => res.json())
                .then(data => {
                    showToast(data.message, data.success);
                    if (!data.success) {
                        loadOrders();
                    }
                })
                .catch(() => {
                    showToast('Lỗi kết nối server', false);
                    loadOrders();
                });
        });
    });
}
function bindOrderRowClick() {
    document.querySelectorAll('.order-row').forEach(row => {
        row.addEventListener('click', function () {
            const orderId = this.dataset.orderId;
            openOrderDetailModal(orderId);
        });
    });
}

function loadOrders() {
    const keyword = searchInput?.value || '';
    const statusFilterValue = statusFilterSelect?.value || '';
    fetch(`${contextPath}/admin/orders?ajax=true&keyword=${encodeURIComponent(keyword)}&statusFilter=${statusFilterValue}`)
        .then(res => {
            if (res.status === 403) {
                showToast('Bạn không có quyền xem đơn hàng', false);
                return null;
            }
            if (!res.ok) {
                showToast('Không tải được danh sách đơn', false);
                return null;
            }
            return res.text();
        })
        .then(html => {
            if (html === null) return;
            tableBody.innerHTML = html;
            bindStatusChange();
            bindOrderRowClick();
        })
        .catch(() => showToast('Lỗi kết nối server', false));
}
searchInput.addEventListener('input', loadOrders);
statusFilterSelect.addEventListener('change', loadOrders);

function showToast(message, success) {
    const toast = document.getElementById('toast');

    toast.className = 'toast';
    toast.textContent = message;

    toast.classList.add('show');
    toast.classList.add(success ? 'success' : 'error');

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

function updateStatusColor(select) {
    switch (select.value) {
        case '1':
            select.style.backgroundColor = '#fef3c7';
            select.style.color = '#f59e0b';
            select.style.borderColor = '#f59e0b';
            break;
        case '2':
            select.style.backgroundColor = '#e0f2fe';
            select.style.color = '#0ea5e9';
            select.style.borderColor = '#0ea5e9';
            break;
        case '3':
            select.style.backgroundColor = '#dcfce7';
            select.style.color = '#16a34a';
            select.style.borderColor = '#16a34a';
            break;
        case '4':
            select.style.backgroundColor = '#fee2e2';
            select.style.color = '#dc2626';
            select.style.borderColor = '#dc2626';
            break;
        default:
            select.style.backgroundColor = '#fff';
            select.style.color = '#333';
            select.style.borderColor = '#ddd';
    }
}
function openOrderDetailModal(orderId) {
    fetch(`${contextPath}/admin/orders?getOrderDetails=true&orderId=${orderId}`)
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                populateOrderDetailModal(data);
                document.getElementById('orderDetailModal').classList.add('show');
            } else {
                showToast(data.message || 'Lỗi khi lấy chi tiết đơn hàng', false);
            }
        })
        .catch(err => {
            console.error('Error:', err);
            showToast('Lỗi kết nối server', false);
        });
}
function closeOrderDetailModal() {
    document.getElementById('orderDetailModal').classList.remove('show');
}
function populateOrderDetailModal(data) {
    const order = data.order;
    const address = data.address;
    const items = data.items || [];
    const statusClass = data.statusClass;
    const statusName = data.statusName;
    document.getElementById('modalOrderId').textContent = order.id;
    document.getElementById('customerNameDetail').textContent = address?.name || 'N/A';
    document.getElementById('customerPhoneDetail').textContent = address?.phoneNumber || 'N/A';
    document.getElementById('customerAddressDetail').textContent = address?.address || 'N/A';
    document.getElementById('orderDateDetail').textContent = formatDate(order.createdAt);
    document.getElementById('orderStatusDetail').innerHTML = `<span class="status-badge ${statusClass}">${statusName}</span>`;
    
    const paymentMethod = order.paymentTypeId === 1 ? 'COD (Thanh toán khi nhận)' : 'Chuyển khoản ngân hàng';
    document.getElementById('paymentMethodDetail').textContent = paymentMethod;
    document.getElementById('orderNoteDetail').textContent = order.note || 'Không có ghi chú';
    const cancellationReasonRow = document.getElementById('cancellationReasonRow');
    if (order.status === 4 && order.cancellationReason) {
        cancellationReasonRow.style.display = 'flex';
        document.getElementById('cancellationReasonDetail').textContent = order.cancellationReason;
    } else {
        cancellationReasonRow.style.display = 'none';
    }
    let itemsHtml = '';
    items.forEach(item => {
        const itemName = `${item.productName}${item.variantName ? ' - ' + item.variantName : ''}${item.colorName ? ' - ' + item.colorName : ''}`;
        itemsHtml += `
            <div class="item-row">
                <div class="item-info">
                    <div class="item-name">${itemName}</div>
                    <div class="item-qty">Số lượng: ${item.quantity}</div>
                </div>
                <div class="item-price">${formatCurrency(item.price)} ₫ x ${item.quantity} = ${formatCurrency(item.totalMoney)} ₫</div>
            </div>
        `;
    });
    document.getElementById('orderItemsDetail').innerHTML = itemsHtml || '<p style="text-align:center; color:#666;">Không có sản phẩm</p>';
    let subtotal = items.reduce((sum, item) => sum + item.totalMoney, 0);
    document.getElementById('subtotalDetail').textContent = formatCurrency(subtotal) + ' ₫';
    document.getElementById('discountDetail').textContent = formatCurrency(order.discountAmount) + ' ₫';
    document.getElementById('shippingFeeDetail').textContent = formatCurrency(order.feeShipping) + ' ₫';
    document.getElementById('totalDetail').textContent = formatCurrency(order.totalAmount) + ' ₫';
    const cancelBtn = document.getElementById('cancelOrderBtn');
    if (order.status === 1 || order.status === 2) {
        cancelBtn.style.display = 'block';
        currentOrderIdForCancel = order.id;
    } else {
        cancelBtn.style.display = 'none';
    }
}
function openCancelReasonModal() {
    closeOrderDetailModal();
    document.getElementById('cancelOrderId').textContent = currentOrderIdForCancel;
    document.getElementById('cancellationReasonInput').value = '';
    document.getElementById('charCount').textContent = '0';
    document.getElementById('cancelReasonModal').classList.add('show');
}
function closeCancelReasonModal() {
    document.getElementById('cancelReasonModal').classList.remove('show');
    document.getElementById('cancellationReasonInput').value = '';
}
function confirmCancelOrder() {
    const reason = document.getElementById('cancellationReasonInput').value.trim();
    if (!reason) {
        showToast('Vui lòng nhập lý do hủy đơn hàng', false);
        return;
    }
    const confirmBtn = document.getElementById('confirmCancelBtn');
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Đang xử lý...';

    fetch(`${contextPath}/admin/orders`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `orderId=${currentOrderIdForCancel}&action=cancelOrder&cancellationReason=${encodeURIComponent(reason)}`
    })
        .then(res => res.json())
        .then(data => {
            showToast(data.message, data.success);
            if (data.success) {
                closeCancelReasonModal();
                loadOrders();
            }
        })
        .catch(err => {
            console.error('Error:', err);
            showToast('Lỗi kết nối server', false);
        })
        .finally(() => {
            confirmBtn.disabled = false;
            confirmBtn.textContent = 'Xác Nhận Hủy';
        });
}
function formatDate(timestamp) {
    if (!timestamp) return 'N/A';
    const date = new Date(timestamp);
    return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN');
}
function formatCurrency(value) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(value));
}
document.addEventListener('DOMContentLoaded', function() {
    const reasonInput = document.getElementById('cancellationReasonInput');
    if (reasonInput) {
        reasonInput.addEventListener('input', function() {
            document.getElementById('charCount').textContent = this.value.length;
        });
    }
});
window.addEventListener('click', function(event) {
    const detailModal = document.getElementById('orderDetailModal');
    const cancelModal = document.getElementById('cancelReasonModal');
    if (event.target === detailModal) {
        closeOrderDetailModal();
    }
    if (event.target === cancelModal) {
        closeCancelReasonModal();
    }
});
bindStatusChange();
bindOrderRowClick();
