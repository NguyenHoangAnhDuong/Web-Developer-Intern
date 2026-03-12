document.addEventListener("DOMContentLoaded", function () {
    const cartButtons = document.querySelectorAll(".btn-cart, .cart-btn");
    const cartCount = document.getElementById("cart-badge");
    let count = parseInt(cartCount.textContent) || 0;
    const cartIconContainer = document.querySelector('#icon-box .cart-item');
    if (!cartIconContainer) {
        return;
    }
    // Tọa độ Giỏ hàng (đích đến)
    const cartIconRect = cartIconContainer.getBoundingClientRect();
    const targetX = cartIconRect.left + cartIconRect.width / 2;
    const targetY = cartIconRect.top + cartIconRect.height / 2;
    cartButtons.forEach(button => {
        button.addEventListener("click", function (e) {
            e.preventDefault();
            let productImage = null;
            if (button.classList.contains('btn-cart')) {
                productImage = document.querySelector('.product-detail .img-feature');
            } else if (button.classList.contains('cart-btn')) {
                const productCard = button.closest('.product-card');
                if (productCard) {
                    productImage = productCard.querySelector('.product-img > img');
                }
            }
            // --- Bắt đầu hiệu ứng bay ---
            if (productImage) {
                const startRect = productImage.getBoundingClientRect();
                const startX = startRect.left;
                const startY = startRect.top;

                const flyingImage = productImage.cloneNode(true);
                flyingImage.classList.add('flying-image');
                flyingImage.style.width = `${startRect.width}px`;
                flyingImage.style.height = `${startRect.height}px`;
                flyingImage.style.top = `${startY + window.scrollY}px`;
                flyingImage.style.left = `${startX + window.scrollX}px`;

                document.body.appendChild(flyingImage);

                const startCenterX = startX + startRect.width / 2;
                const startCenterY = startY + startRect.height / 2;

                const offsetX = targetX - startCenterX;
                const offsetY = targetY - startCenterY;

                setTimeout(() => {
                    // Áp dụng di chuyển (translate) và thu nhỏ (scale)
                    flyingImage.style.transform = `translate(${offsetX}px, ${offsetY}px) scale(0.1)`;
                    flyingImage.style.opacity = '0';
                    setTimeout(() => {
                        count++;
                        cartCount.textContent = count;
                        flyingImage.remove();
                    }, 750);
                }, 50);
            } else {
                count++;
                cartCount.textContent = count;
            }
        });
    });
});