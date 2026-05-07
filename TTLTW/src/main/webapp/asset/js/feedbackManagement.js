const ctx = document.querySelector('meta[name="context-path"]')?.content || '';
document.querySelectorAll('.btn-approve:not(#d-approve-link)').forEach(btn => {
    btn.addEventListener('click', e => {
        e.preventDefault();
        showConfirmToast('Duyệt đánh giá này?', () => window.location.href = btn.href);
    });
});
document.querySelectorAll('.btn-hide:not(#d-hide-link)').forEach(btn => {
    btn.addEventListener('click', e => {
        e.preventDefault();
        showConfirmToast('Ẩn đánh giá này?', () => window.location.href = btn.href);
    });
});
document.querySelectorAll('.btn-delete:not(#d-delete-link)').forEach(btn => {
    btn.addEventListener('click', e => {
        e.preventDefault();
        showConfirmToast('Xóa vĩnh viễn đánh giá này?', () => window.location.href = btn.href);
    });
});
document.querySelectorAll('.btn-view').forEach(btn => {
    btn.addEventListener('click', () => {
        openDetail(
            btn.dataset.username,
            btn.dataset.product,
            parseInt(btn.dataset.rating),
            btn.dataset.comment,
            btn.dataset.date,
            parseInt(btn.dataset.status),
            parseInt(btn.dataset.id)
        );
    });
});
function openDetail(username, productName, rating, comment, createdAt, status, id) {
    document.getElementById('d-user').textContent    = username || 'Không rõ';
    document.getElementById('d-product').textContent = productName || 'Không rõ';
    document.getElementById('d-comment').textContent = comment || '';
    document.getElementById('d-date').textContent    = createdAt || '';
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        stars += i <= rating ? '★' : '☆';
    }
    const starClass = rating <= 2 ? 'red' : rating === 3 ? 'amber' : 'green';
    document.getElementById('d-star').innerHTML =
        `<span style="color:${starClass === 'red' ? '#E24B4A' : starClass === 'amber' ? '#BA7517' : '#3B6D11'};
            font-size:16px;letter-spacing:1px">${stars}</span> (${rating}/5)`;
    // Hiển thị badge trạng thái
    const statusMap = { 0: ['Đã ẩn', 'badge-hide'], 1: ['Hiển thị', 'badge-show'], 2: ['Chờ duyệt', 'badge-pending'] };
    const [label, cls] = statusMap[status] || ['Không rõ', ''];
    document.getElementById('d-status').innerHTML = `<span class="badge ${cls}">${label}</span>`;
    // Cập nhật link hành động
    const base = `${ctx}/admin/feedbacks`;
    const approveLink = document.getElementById('d-approve-link');
    const hideLink    = document.getElementById('d-hide-link');
    const deleteLink  = document.getElementById('d-delete-link');
    const approveHref = `${base}?action=approve&id=${id}`;
    const hideHref   = `${base}?action=hide&id=${id}`;
    const deleteHref  = `${base}?action=delete&id=${id}`;
    if (approveLink) {
        approveLink.onclick = (e) => {
            e.preventDefault();
            showConfirmToast('Duyệt đánh giá này?', () => window.location.href = approveHref);
        };
        approveLink.style.display = status !== 1 ? 'inline-flex' : 'none';
    }
    if (hideLink) {
        hideLink.onclick = (e) => {
            e.preventDefault();
            showConfirmToast('Ẩn đánh giá này?', () => window.location.href = hideHref);
        };
        hideLink.style.display = status !== 0 ? 'inline-flex' : 'none';
    }
    if (deleteLink) {
        deleteLink.onclick = (e) => {
            e.preventDefault();
            showConfirmToast('Xóa vĩnh viễn đánh giá này?', () => window.location.href = deleteHref);
        };
    }
    document.getElementById('modal-overlay').classList.add('open');
}
function closeDetail() {
    document.getElementById('modal-overlay').classList.remove('open');
}
// Đóng modal khi nhấn Escape
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') closeDetail();
});
function showConfirmToast(message, onConfirm) {
    const old = document.getElementById('confirm-toast');
    if (old) old.remove();
    const el = document.createElement('div');
    el.id = 'confirm-toast';
    el.className = 'toast confirm-toast show';
    el.style.cssText = `
        position:fixed; top:20px; right:30px; min-width:280px;
        padding:14px 18px; border-radius:8px; color:#fff;
        font-size:14px; font-weight:500; z-index:9999999;
        background:#1e293b; box-shadow:0 8px 24px rgba(0,0,0,0.2);
        display:flex; flex-direction:column; gap:10px;
    `;
    el.innerHTML = `
        <span>${message}</span>
        <div style="display:flex;gap:8px;justify-content:flex-end">
            <button id="ct-cancel" style="padding:5px 14px;border-radius:6px;border:none;
                background:#475569;color:#fff;cursor:pointer;font-size:13px">Hủy</button>
            <button id="ct-ok" style="padding:5px 14px;border-radius:6px;border:none;
                background:#ef4444;color:#fff;cursor:pointer;font-size:13px">Xác nhận</button>
        </div>
    `;
    document.body.appendChild(el);
    document.getElementById('ct-cancel').onclick = () => el.remove();
    document.getElementById('ct-ok').onclick = () => {
        el.remove();
        onConfirm();
    };
}
function showToast(message, type = 'success') {
    const old = document.getElementById('toast');
    if (old) old.remove();
    const toast = document.createElement('div');
    toast.id = 'toast';
    toast.className = `toast ${type}`;
    toast.innerText = message;
    document.body.appendChild(toast);

    setTimeout(() => toast.classList.add('show'), 100);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 3500);
}