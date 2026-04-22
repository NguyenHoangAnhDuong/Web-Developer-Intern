const ctx = document.querySelector('meta[name="context-path"]')?.content || '';
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
    approveLink.href = `${base}?action=approve&id=${id}`;
    hideLink.href    = `${base}?action=hide&id=${id}`;
    deleteLink.href  = `${base}?action=delete&id=${id}`;
    // Ẩn nút không cần thiết
    approveLink.style.display = status !== 1 ? 'inline-flex' : 'none';
    hideLink.style.display    = status !== 0 ? 'inline-flex' : 'none';
    document.getElementById('modal-overlay').classList.add('open');
}
function closeDetail() {
    document.getElementById('modal-overlay').classList.remove('open');
}
// Đóng modal khi nhấn Escape
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') closeDetail();
});