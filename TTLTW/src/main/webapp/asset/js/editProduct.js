document.addEventListener('DOMContentLoaded', function() {
    const addButtons = document.querySelectorAll('.btn-add-minor');
    addButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Tìm vùng chứa danh sách thông số cùng cấp với nút vừa bấm
            const card = this.closest('.card');
            const list = card.querySelector('.tech-specs-list');
            // Tạo hàng thông số mới
            const newRow = document.createElement('div');
            newRow.className = 'tech-row';
            newRow.innerHTML = `
                <input name="techNames[]" class="col-name" placeholder="Tên thông số (VD: Pin)">
                <input name="techValues[]" class="col-value" placeholder="Giá trị (VD: 5000mAh)">
                <input name="techPriorities[]" class="col-priority" type="number" value="1">
                <button type="button" class="btn-remove-tech">✕</button>
            `;
            // Thêm vào danh sách
            list.appendChild(newRow);
            // Gán sự kiện xóa cho nút vừa tạo
            const removeBtn = newRow.querySelector('.btn-remove-tech');
            removeBtn.addEventListener('click', function() {
                newRow.remove();
            });
        });
    });

    // Gán sự kiện xóa cho các thông số ĐÃ CÓ SẴN khi load trang
    const existingRemoveButtons = document.querySelectorAll('.btn-remove-tech');
    existingRemoveButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            this.closest('.tech-row').remove();
        });
    });
});