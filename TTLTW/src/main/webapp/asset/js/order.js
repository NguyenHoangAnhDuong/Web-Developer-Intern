function cancelOrder(orderId) {
    showConfirmDialog('Bạn có chắc chắn muốn hủy đơn hàng này?', 'Xác nhận hủy đơn', {
        iconType: 'warning',
        confirmText: 'Hủy',
        cancelText: 'Không',
        confirmButtonClass: 'btn-confirm-delete'
    }).then(confirmed => {
        if (!confirmed) return;
        
        const contextPath = document.body.getAttribute('data-context-path') || '';
        fetch(contextPath + '/api/order/cancel', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'action=cancel&orderId=' + orderId
        })
            .then(response => response.json())
            .then(data => {
                if (data.success || data.status === 'success') {
                    setTimeout(() => {
                        showToast(data.message || 'Hủy đơn hàng thành công!', 'success', 3000);
                        setTimeout(() => location.reload(), 1500);
                    }, 300);
                } else {
                    setTimeout(() => {
                        showToast(data.message || 'Không thể hủy đơn hàng', 'error', 3000);
                    }, 300);
                }
            })
            .catch(error => {
                setTimeout(() => {
                    showToast('Có lỗi xảy ra: ' + error, 'error', 3000);
                }, 300);
            });
    });
}

// Mua lại
function repurchaseOrder(orderId) {
    showConfirmDialog('Bạn có muốn mua lại đơn hàng này?', 'Xác nhận mua lại', {
        iconType: 'info',
        confirmText: 'Mua lại',
        cancelText: 'Không',
        confirmButtonClass: 'btn-confirm'
    }).then(confirmed => {
        if (!confirmed) return;
        
        const contextPath = document.body.getAttribute('data-context-path') || '';
        fetch(contextPath + '/user/order', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'action=repurchase&orderId=' + orderId
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    setTimeout(() => {
                        showToast('Sản phẩm đã được thêm vào giỏ hàng!', 'success', 3000);
                        setTimeout(() => {
                            if (data.redirectUrl) {
                                location.href = data.redirectUrl;
                            } else {
                                const cartUrl = contextPath + '/user/cart' || '/user/cart';
                                location.href = cartUrl;
                            }
                        }, 1500);
                    }, 300);
                } else {
                    setTimeout(() => {
                        showToast(data.message || 'Không thể mua lại đơn hàng', 'error', 3000);
                    }, 300);
                }
            })
            .catch(error => {
                setTimeout(() => {
                    showToast('Có lỗi xảy ra: ' + error, 'error', 3000);
                }, 300);
            });
    });
}
const menuAccountMain = document.getElementById("menuAccountMain");
const accountSubmenu = document.getElementById("accountSubmenu");

if (menuAccountMain && accountSubmenu) {
    accountSubmenu.classList.add("open");
    menuAccountMain.addEventListener("click", (e) => {
        e.preventDefault();
        accountSubmenu.classList.toggle("open");
    });
}
