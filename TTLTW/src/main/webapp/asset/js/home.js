
document.addEventListener("DOMContentLoaded", () => {
    let slideIndex = 0;
    const slideEls = Array.from(document.querySelectorAll(".slide"));
    const nextBtn = document.querySelector(".next");
    const prevBtn = document.querySelector(".prev");

    function showSlide(i) {
        const len = slideEls.length;
        if (len === 0) return;
        slideIndex = ((i % len) + len) % len;
        slideEls.forEach((slide, idx) => {
            slide.classList.toggle("active", idx === slideIndex);
        });
    }
    if (slideEls.length > 0) {
        let autoTimer = setInterval(() => showSlide(slideIndex + 1), 4000);
        if (nextBtn) {
            nextBtn.onclick = () => {
                clearInterval(autoTimer);
                showSlide(slideIndex + 1);
                autoTimer = setInterval(() => showSlide(slideIndex + 1), 4000);
            };
        }
        if (prevBtn) {
            prevBtn.onclick = () => {
                clearInterval(autoTimer);
                showSlide(slideIndex - 1);
                autoTimer = setInterval(() => showSlide(slideIndex + 1), 4000);
            };
        }
        showSlide(0);
    }
    function updateCardVariantId(productCard) {
        const activeVariant = productCard.querySelector('.capacity button.active');
        const activeColor = productCard.querySelector('.colors .color.active');
        if (!activeVariant) return;
        const colorMapping = JSON.parse(activeVariant.getAttribute('data-color-mapping'));
        if (activeColor) {
            const colorId = activeColor.getAttribute('data-color-id');
            const vcId = colorId && colorMapping[colorId] ? colorMapping[colorId] : null;
            if (vcId) {
                productCard.setAttribute('data-variant-color-id', vcId);
            }
        } else {
            // Nếu không có màu, lấy ID đầu tiên từ mapping
            const firstVcId = Object.values(colorMapping)[0];
            if (firstVcId) {
                productCard.setAttribute('data-variant-color-id', firstVcId);
            }
        }
    }

    // Hàm cập nhật danh sách màu khi chọn variant khác
    function updateColorsForVariant(productCard, variantId) {
        const variantColorsData = productCard.querySelector('.variant-colors-data');
        const colorsContainer = productCard.querySelector('.colors');

        if (!variantColorsData || !colorsContainer) return;

        const colorGroup = variantColorsData.querySelector(`[data-variant-id="${variantId}"]`);
        
        if (colorGroup) {
            // Xóa tất cả màu cũ
            colorsContainer.innerHTML = '';
            
            // Lấy tất cả color items từ variant này
            const colorItems = colorGroup.querySelectorAll('.color-item');
            colorItems.forEach((item, index) => {
                const colorBtn = document.createElement('button');
                colorBtn.className = `color ${index === 0 ? 'active' : ''}`;
                colorBtn.setAttribute('data-color', item.getAttribute('data-color'));
                colorBtn.setAttribute('data-color-id', item.getAttribute('data-color-id'));
                colorBtn.setAttribute('data-color-code', item.getAttribute('data-color-code'));
                colorBtn.setAttribute('data-variant-color-id', item.getAttribute('data-variant-color-id'));
                colorBtn.setAttribute('data-color-price-new', item.getAttribute('data-color-price-new'));
                colorBtn.setAttribute('data-color-price-old', item.getAttribute('data-color-price-old'));
                colorBtn.setAttribute('title', item.getAttribute('data-color'));
                colorBtn.style.backgroundColor = item.getAttribute('data-color-code');
                
                colorBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    colorsContainer.querySelectorAll('.color').forEach(c => c.classList.remove('active'));
                    colorBtn.classList.add('active');
                    
                    // Cập nhật giá khi chọn màu khác
                    const productId = productCard.querySelector('.capacity button').getAttribute('data-product-id');
                    const colorPriceNew = colorBtn.getAttribute('data-color-price-new');
                    const colorPriceOld = colorBtn.getAttribute('data-color-price-old');
                    if (colorPriceNew && colorPriceOld) {
                        updatePrice(productCard, colorPriceNew, colorPriceOld);
                    }
                    
                    updateCardVariantId(productCard);
                });
                
                colorsContainer.appendChild(colorBtn);
            });
            updateCardVariantId(productCard);
            // Cập nhật giá dựa trên màu đầu tiên
            if (colorItems.length > 0) {
                const firstColorPriceNew = colorItems[0].getAttribute('data-color-price-new');
                const firstColorPriceOld = colorItems[0].getAttribute('data-color-price-old');
                if (firstColorPriceNew && firstColorPriceOld) {
                    updatePrice(productCard, firstColorPriceNew, firstColorPriceOld);
                }
            }
        }
    }
    // Hàm cập nhật giá
    function updatePrice(productCard, newPrice, oldPrice) {
        const productId = productCard.querySelector('.capacity button')?.getAttribute('data-product-id');
        if (!productId) return;
        
        const priceNewEl = document.getElementById('price-new-' + productId);
        const priceOldEl = document.getElementById('price-old-' + productId);
        
        if (priceNewEl && !isNaN(newPrice)) {
            priceNewEl.textContent = formatCurrency(parseFloat(newPrice));
        }
        
        if (priceOldEl && !isNaN(oldPrice)) {
            priceOldEl.textContent = formatCurrency(parseFloat(oldPrice));
            priceOldEl.style.display = 'inline-block';
        } else if (priceOldEl) {
            priceOldEl.style.display = 'none';
        }
    }

    document.querySelectorAll('.capacity button').forEach(button => {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            const productCard = this.closest('.product-card');
            const productId = this.getAttribute('data-product-id');
            const variantId = this.getAttribute('data-variant-id');

            // Cập nhật giá
            const newPrice = parseFloat(this.getAttribute('data-price'));
            const oldPrice = parseFloat(this.getAttribute('data-old-price'));

            const priceNewEl = document.getElementById('price-new-' + productId);
            const priceOldEl = document.getElementById('price-old-' + productId);

            if (priceNewEl && !isNaN(newPrice)) {
                priceNewEl.textContent = formatCurrency(newPrice);
            }

            if (priceOldEl && !isNaN(oldPrice)) {
                priceOldEl.textContent = formatCurrency(oldPrice);
                priceOldEl.style.display = 'inline-block';
            } else if (priceOldEl) {
                priceOldEl.style.display = 'none';
            }
            const parent = this.parentElement;
            parent.querySelectorAll('button').forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            // Cập nhật danh sách màu cho variant này
            updateColorsForVariant(productCard, variantId);
        });
    });
    function formatCurrency(number) {
        return new Intl.NumberFormat('vi-VN').format(Math.round(number)) + '₫';
    }

    function setupNavigation(listId, prevBtnId, nextBtnId) {
        const listEl = document.getElementById(listId);
        const btnPrev = document.getElementById(prevBtnId);
        const btnNext = document.getElementById(nextBtnId);

        if (!listEl || !btnPrev || !btnNext) return;

        const cards = listEl.querySelectorAll('.product-card');
        let currentIndex = 0;
        const perPage = 4;

        function updateDisplay(start) {
            cards.forEach((card, i) => {
                card.style.display = (i >= start && i < start + perPage) ? 'block' : 'none';
            });
            btnPrev.disabled = (start === 0);
            btnNext.disabled = (start + perPage >= cards.length);
        }

        btnNext.onclick = () => {
            if (currentIndex + perPage < cards.length) {
                currentIndex += perPage;
                updateDisplay(currentIndex);
            }
        };

        btnPrev.onclick = () => {
            if (currentIndex > 0) {
                currentIndex -= perPage;
                updateDisplay(currentIndex);
            }
        };

        updateDisplay(0); // Khởi tạo trạng thái ban đầu
    }

    setupNavigation('product-list-phones', 'phones-prev', 'phones-next');
    setupNavigation('product-list-accessories', 'accessories-prev', 'accessories-next');

    const cartButtons = document.querySelectorAll('.cart-btn.add-to-cart');
    cartButtons.forEach(btn => {
        btn.onclick = function (e) {
            e.preventDefault();
            e.stopPropagation();
            const productCard = btn.closest('.product-card');
            // Kiểm tra đã chọn variant chưa
            const activeVariant = productCard.querySelector('.capacity button.active');
            if (!activeVariant) {
                alert("Vui lòng chọn phiên bản sản phẩm!");
                return;
            }
            // Kiểm tra đã chọn màu chưa (nếu có màu)
            const hasColors = productCard.querySelector('.colors .color');
            if (hasColors) {
                const activeColor = productCard.querySelector('.colors .color.active');
                if (!activeColor) {
                    alert("Vui lòng chọn màu sản phẩm!");
                    return;
                }
            }
            let vcId = productCard.getAttribute('data-variant-color-id');

            if (!vcId) {
                updateCardVariantId(productCard);
                vcId = productCard.getAttribute('data-variant-color-id');
            }

            if (!vcId) {
                alert("Không tìm thấy mã phiên bản sản phẩm. Vui lòng thử lại!");
                return;
            }

            const header = document.getElementById('header');
            const contextPath = header ? header.getAttribute('data-context-path') : '';

            fetch(`${contextPath}/cart?action=add&vcId=${vcId}`)
                .then(response => {
                    if (response.status === 401) {
                        alert("Vui lòng đăng nhập để thêm vào giỏ hàng!");
                        window.location.href = contextPath + "/login";
                        throw new Error("UNAUTHORIZED");
                    }
                    if (!response.ok) throw new Error("SERVER_ERROR");
                    return response.text();
                })
                .then(totalCount => {
                    const cartBadge = document.getElementById('cart-badge');
                    if (cartBadge && totalCount) {
                        cartBadge.textContent = totalCount.trim();
                    }
                    alert("Đã thêm vào giỏ hàng!");
                })
                .catch(err => {
                    if (err.message !== "UNAUTHORIZED") {
                        console.error("Lỗi thêm vào giỏ hàng:", err);
                        alert("Lỗi khi thêm vào giỏ hàng. Vui lòng thử lại!");
                    }
                });
        };
    });
    document.querySelectorAll('.colors .color').forEach(color => {
        const colorCode = color.getAttribute('data-color-code');
        if (colorCode) {
            color.style.backgroundColor = colorCode;
        }

        color.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();

            const productCard = color.closest('.product-card');
            const colorsContainer = color.closest('.colors');

            // Bỏ active khỏi tất cả màu
            colorsContainer.querySelectorAll('.color').forEach(c => c.classList.remove('active'));
            color.classList.add('active');

            // Cập nhật giá khi chọn màu khác
            const colorPriceNew = color.getAttribute('data-color-price-new');
            const colorPriceOld = color.getAttribute('data-color-price-old');
            if (colorPriceNew && colorPriceOld) {
                updatePrice(productCard, colorPriceNew, colorPriceOld);
            }

            // Cập nhật variant_color_id dựa trên màu mới
            updateCardVariantId(productCard);
        });
    });

    document.querySelectorAll('.product-card').forEach(card => {
        // Khởi tạo variant_color_id dựa trên active variant + color
        const activeVariant = card.querySelector('.capacity button.active');
        if (activeVariant) {
            const variantId = activeVariant.getAttribute('data-variant-id');
            updateColorsForVariant(card, variantId);
        }
    });
});