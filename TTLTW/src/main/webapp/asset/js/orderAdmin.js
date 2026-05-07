const searchInput = document.getElementById('searchInput');
const statusFilterSelect = document.getElementById('statusFilter');
const tableBody = document.querySelector('#ordersTable tbody');
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
bindStatusChange();
