document.addEventListener("DOMContentLoaded", () => {
    initFilterSystem();
    initSortSystem();
    initProductCards();
    initLoadMore();
    initCartSystem();
});

function initFilterSystem() {
    const filterItems = document.querySelectorAll('.filter-item');
    const dropdowns = document.querySelectorAll('.dropdown');

    // Khi click vào nút lọc
    filterItems.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const id = btn.getAttribute('data-filter');
            const dropdown = document.getElementById(id);
            // Toggle active cho nút hiện tại
            const isActive = btn.classList.contains('active');
            closeAllFilters();
            if (!isActive) {
                btn.classList.add('active');
                dropdown.classList.add('active');
                // Căn vị trí dropdown khớp với nút lọc
                positionDropdown(btn, dropdown);
            }
        });
    });
    // Xử lý button trong option-group (toggle multiple selection)
    document.querySelectorAll('.option-group button').forEach(btn => {
        btn.addEventListener('click', () => {
            btn.classList.toggle('active');
        });
    });
    // Nút đóng
    document.querySelectorAll('.btn-close').forEach(btn => {
        btn.addEventListener('click', () => {
            closeAllFilters();
        });
    });
    // Nút áp dụng
    document.querySelectorAll('.btn-apply').forEach(btn => {
        btn.addEventListener('click', () => {
            applyFilters();
            closeAllFilters();
        });
    });
    // Click bên ngoài để đóng
    window.addEventListener('click', (e) => {
        if (!e.target.closest('.filter-item') && !e.target.closest('.dropdown')) {
            closeAllFilters();
        }
    });
    // Xử lý chọn thương hiệu
    document.querySelectorAll('.brand').forEach(brand => {
        brand.addEventListener('click', () => {
            document.querySelectorAll('.brand').forEach(b => b.classList.remove('active'));
            brand.classList.add('active');
            // Áp dụng lọc thương hiệu ngay lập tức - lấy tên brand từ alt text
            const brandName = brand.querySelector('img')?.alt || '';
            if (brandName) {
                const url = new URL(window.location.href);
                url.searchParams.set('brandName', brandName);
                window.location.href = url.toString();
            }
        });
    });
    // Khôi phục trạng thái brand từ URL
    const urlParams = new URLSearchParams(window.location.search);
    const currentBrandName = urlParams.get('brandName');
    if (currentBrandName) {
        document.querySelectorAll('.brand').forEach(brand => {
            const brandName = brand.querySelector('img')?.alt || '';
            if (brandName === currentBrandName) {
                brand.classList.add('active');
            }
        });
    }
    // Xử lý chọn category tag (cho linh kiện)
    document.querySelectorAll('.category').forEach(category => {
        category.addEventListener('click', () => {
            const isActive = category.classList.contains('active');
            // Toggle active
            if (isActive) {
                category.classList.remove('active');
            } else {
                category.classList.add('active');
            }
            // Áp dụng filter ngay lập tức
            applyCategoryFilters();
        });
    });
    // Khôi phục trạng thái category từ URL
    const currentCategoryNames = urlParams.getAll('categoryName');
    if (currentCategoryNames && currentCategoryNames.length > 0) {
        document.querySelectorAll('.category').forEach(category => {
            const categoryName = category.getAttribute('data-category-name');
            if (currentCategoryNames.includes(categoryName)) {
                category.classList.add('active');
            }
        });
    }
}
function positionDropdown(btn, dropdown) {
    const filterBarRect = document.querySelector('.filter-options').getBoundingClientRect();
    const btnRect = btn.getBoundingClientRect();
    const leftOffset = btnRect.left - filterBarRect.left;
    dropdown.style.left = `${leftOffset}px`;
}
function closeAllFilters() {
    document.querySelectorAll('.dropdown').forEach(d => d.classList.remove('active'));
    document.querySelectorAll('.filter-item').forEach(b => b.classList.remove('active'));
}
function applyFilters() {
    // Thu thập các filter đã chọn
    const priceRange = getPriceRange();
    const memory = getSelectedOptions('bonho');
    const colors = getSelectedOptions('mausac');
    const year = getSelectedOptions('namramat');
    const type = getSelectedOptions('loai');
    const brand = getSelectedBrand();
    const condition = getSelectedOptions('tinhtrang');
    const model = getSelectedOptions('model');
    // Xây dựng URL với các tham số
    const url = new URL(window.location.href);
    // Xóa các tham số cũ
    url.searchParams.delete('priceMin');
    url.searchParams.delete('priceMax');
    url.searchParams.delete('memory');
    url.searchParams.delete('color');
    url.searchParams.delete('year');
    url.searchParams.delete('type');
    url.searchParams.delete('brandName');
    url.searchParams.delete('condition');
    url.searchParams.delete('model');
    // Thêm các tham số mới
    if (priceRange.min) {
        url.searchParams.set('priceMin', priceRange.min);
    }
    if (priceRange.max) {
        url.searchParams.set('priceMax', priceRange.max);
    }
    if (memory && memory.length > 0) {
        memory.forEach(m => url.searchParams.append('memory', m));
    }
    if (colors && colors.length > 0) {
        colors.forEach(c => url.searchParams.append('color', c));
    }
    if (year && year.length > 0) {
        url.searchParams.set('year', year[0]); // Chỉ lấy năm đầu tiên
    }
    if (type && type.length > 0) {
        type.forEach(t => url.searchParams.append('type', t));
    }
    if (brand) {
        url.searchParams.set('brandName', brand);
    }
    if (condition && condition.length > 0) {
        url.searchParams.set('condition', condition[0]);
    }
    if (model && model.length > 0) {
        model.forEach(m => url.searchParams.append('model', m));
    }
    // Giữ nguyên sort nếu có
    const currentSort = url.searchParams.get('sort');
    if (!currentSort) {
        url.searchParams.delete('sort');
    }
    // Reload trang với các tham số mới
    window.location.href = url.toString();
}
// Hàm áp dụng filter khi click vào category tag
function applyCategoryFilters() {
    // Thu thập các category đang active
    const activeCategories = Array.from(document.querySelectorAll('.category.active'))
        .map(cat => cat.getAttribute('data-category-name'))
        .filter(name => name !== null);
    // Thu thập các filter khác
    const priceRange = getPriceRange();
    const brand = getSelectedBrand();
    const brandFromDropdown = getSelectedOptions('thuonghieu');
    const model = getSelectedOptions('model');
    // Xây dựng URL với các tham số
    const url = new URL(window.location.href);
    // Xóa các tham số cũ
    url.searchParams.delete('priceMin');
    url.searchParams.delete('priceMax');
    url.searchParams.delete('categoryName');
    url.searchParams.delete('brandName');
    url.searchParams.delete('model');
    // Thêm các tham số mới
    if (priceRange.min) {
        url.searchParams.set('priceMin', priceRange.min);
    }
    if (priceRange.max) {
        url.searchParams.set('priceMax', priceRange.max);
    }
    if (activeCategories.length > 0) {
        activeCategories.forEach(name => url.searchParams.append('categoryName', name));
    }
    if (brand) {
        url.searchParams.set('brandName', brand);
    } else if (brandFromDropdown && brandFromDropdown.length > 0) {
        // Nếu không có brand từ brand-list, lấy từ dropdown
        const brandName = brandFromDropdown[0];
        if (brandName) {
            url.searchParams.set('brandName', brandName);
        }
    }
    if (model && model.length > 0) {
        model.forEach(m => url.searchParams.append('model', m));
    }

    // Giữ nguyên sort nếu có
    const currentSort = url.searchParams.get('sort');
    if (!currentSort) {
        url.searchParams.delete('sort');
    }

    // Reload trang với các tham số mới
    window.location.href = url.toString();
}

function getPriceRange() {
    const priceInputs = document.querySelectorAll('#gia input[type="number"]');
    return {
        min: priceInputs[0]?.value || null,
        max: priceInputs[1]?.value || null
    };
}

function getSelectedOptions(filterId) {
    const dropdown = document.getElementById(filterId);
    if (!dropdown) return null;
    const activeButtons = dropdown.querySelectorAll('.option-group button.active');
    const options = Array.from(activeButtons).map(btn => btn.textContent.trim());
    return options.length > 0 ? options : null;
}

function getSelectedBrand() {
    const activeBrand = document.querySelector('.brand.active');
    if (!activeBrand) return null;
    // Lấy brand name từ alt text của image
    const brandName = activeBrand.querySelector('img')?.alt || '';
    return brandName || null;
}
function initSortSystem() {
    const sortItems = document.querySelectorAll("#sortList li");

    // Khôi phục trạng thái sort từ URL
    const urlParams = new URLSearchParams(window.location.search);
    const currentSort = urlParams.get('sort');
    if (currentSort) {
        const sortMap = {
            'banchay': 'Bán chạy',
            'giamgia': 'Giảm giá',
            'moi': 'Mới',
            'giatang': 'Giá',
            'giagiam': 'Giá'
        };
        const sortText = sortMap[currentSort] || 'Nổi bật';
        sortItems.forEach(li => {
            if (li.textContent.trim().includes(sortText)) {
                li.classList.add('active');
            }
        });
    }

    sortItems.forEach(item => {
        item.addEventListener("click", () => {
            sortItems.forEach(li => li.classList.remove("active"));
            item.classList.add("active");

            const sortType = item.textContent.trim();
            applySort(sortType);
        });
    });
}

function applySort(sortType) {
    // Xây dựng URL với tham số sort
    const url = new URL(window.location.href);
    
    // Map sort type to parameter value
    let sortParam = '';
    switch(sortType) {
        case 'Bán chạy':
            sortParam = 'banchay';
            break;
        case 'Giảm giá':
            sortParam = 'giamgia';
            break;
        case 'Mới':
            sortParam = 'moi';
            break;
        case 'Giá':
            // Toggle giá tăng/giảm
            const arrow = document.querySelector('#sortList li:last-child .arrow');
            const currentSort = url.searchParams.get('sort');
            if (arrow && arrow.classList.contains('asc') || currentSort === 'giatang') {
                arrow.classList.remove('asc');
                sortParam = 'giagiam';
            } else {
                if (arrow) arrow.classList.add('asc');
                sortParam = 'giatang';
            }
            break;
        default: // Nổi bật
            sortParam = '';
            break;
    }

    if (sortParam) {
        url.searchParams.set('sort', sortParam);
    } else {
        url.searchParams.delete('sort');
    }

    // Reload trang với tham số sort mới
    window.location.href = url.toString();
}

// Hàm sort trên client (fallback nếu cần)
function sortProducts(sortType) {
    const productList = document.getElementById('product-list');
    const products = Array.from(productList.querySelectorAll('.product-card'));

    products.sort((a, b) => {
        switch(sortType) {
            case 'Bán chạy':
                return getSoldCount(b) - getSoldCount(a);
            case 'Giảm giá':
                return getDiscount(b) - getDiscount(a);
            case 'Giá':
                // Toggle giá tăng/giảm
                const arrow = document.querySelector('#sortList li:last-child .arrow');
                if (arrow.classList.contains('asc')) {
                    arrow.classList.remove('asc');
                    return getPrice(b) - getPrice(a); // Giảm dần
                } else {
                    arrow.classList.add('asc');
                    return getPrice(a) - getPrice(b); // Tăng dần
                }
            case 'Mới':
                // Giả sử sản phẩm mới nhất ở đầu danh sách
                return 0;
            default: // Nổi bật
                return 0;
        }
    });

    // Re-render products
    products.forEach(product => productList.appendChild(product));
}

function getSoldCount(card) {
    const text = card.querySelector('.sold-count')?.textContent || '0';
    return parseInt(text.replace(/\D/g, '')) || 0;
}

function getDiscount(card) {
    const text = card.querySelector('.discount-badge')?.textContent || '0';
    return parseInt(text.replace(/\D/g, '')) || 0;
}

function getPrice(card) {
    const text = card.querySelector('.price-new')?.textContent || '0';
    return parseInt(text.replace(/\D/g, '')) || 0;
}

function initProductCards() {
    const productCards = document.querySelectorAll('.product-card');

    productCards.forEach(card => {
        initCapacitySelection(card);
        initColorSelection(card);
    });
}

function initCapacitySelection(card) {
    const capacityButtons = card.querySelectorAll('.capacity button');
    const colorsContainer = card.querySelector('.colors');
    const variantColorsData = card.querySelector('.variant-colors-data');

    capacityButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            capacityButtons.forEach(b => b.classList.remove('active'));

            // Thêm active vào nút được click
            btn.classList.add('active');

            // Lấy giá từ data attributes
            const newPrice = btn.getAttribute('data-price');
            const oldPrice = btn.getAttribute('data-old-price');

            // Cập nhật giá hiển thị
            if (newPrice) {
                updatePrice(card, newPrice, oldPrice);
            }

            // Cập nhật màu sắc khi chọn phiên bản
            if (variantColorsData && colorsContainer) {
                const variantId = btn.getAttribute('data-id');
                const colorGroup = variantColorsData.querySelector(`[data-variant-id="${variantId}"]`);
                
                if (colorGroup) {
                    // Xóa tất cả màu cũ
                    colorsContainer.innerHTML = '';
                    
                    // Thêm màu của phiên bản hiện tại
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
                            const colorName = colorBtn.getAttribute('data-color');
                            const colorId = colorBtn.getAttribute('data-color-id');
                            const variantColorId = colorBtn.getAttribute('data-variant-color-id');
                            let colorPriceNew = colorBtn.getAttribute('data-color-price-new');
                            let colorPriceOld = colorBtn.getAttribute('data-color-price-old');
                            console.log('Màu đã chọn:', colorName, 'Color ID:', colorId, 'Variant Color ID:', variantColorId, 'Giá mới:', colorPriceNew);
                            card.setAttribute('data-selected-color-id', colorId);
                            card.setAttribute('data-variant-color-id', variantColorId);
                            
                            // Cập nhật giá nếu màu có giá khác
                            if (colorPriceNew && colorPriceNew !== 'undefined' && colorPriceNew !== '') {
                                updatePrice(card, colorPriceNew, colorPriceOld);
                            } else {
                                // Nếu màu không có giá riêng, dùng giá variant hiện tại
                                updatePrice(card, newPrice, oldPrice);
                                console.log('Dùng giá variant:', newPrice);
                            }
                        });
                        
                        colorsContainer.appendChild(colorBtn);
                    });
                    
                    // Set selected color id và giá cho card dựa trên màu đầu tiên
                    if (colorItems.length > 0) {
                        const firstColorId = colorItems[0].getAttribute('data-color-id');
                        const firstVcId = colorItems[0].getAttribute('data-variant-color-id');
                        card.setAttribute('data-selected-color-id', firstColorId);
                        card.setAttribute('data-variant-color-id', firstVcId);
                        const firstColorPriceNew = colorItems[0].getAttribute('data-color-price-new');
                        const firstColorPriceOld = colorItems[0].getAttribute('data-color-price-old');
                        card.setAttribute('data-selected-color-id', firstColorId);
                        // Cập nhật giá: nếu màu có giá riêng thì dùng giá đó, nếu không thì dùng giá variant
                        if (firstColorPriceNew && firstColorPriceNew !== 'undefined' && firstColorPriceNew !== '') {
                            updatePrice(card, firstColorPriceNew, firstColorPriceOld);
                        } else {
                            updatePrice(card, newPrice, oldPrice);
                            console.log('Dùng giá variant khi chọn:', newPrice);
                        }
                    }
                    
                    // Gọi lại initColorSelection để gắn event listeners cho các button màu mới
                    initColorSelection(card);
                }
            }

            console.log('Dung lượng đã chọn:', btn.textContent.trim());
        });
    });
}

function initColorSelection(card) {
    const colors = card.querySelectorAll('.colors .color');
    // Lấy giá variant hiện tại
    const currentPriceNewElement = card.querySelector('.price-new');
    const currentPriceNew = currentPriceNewElement?.textContent?.replace(/[₫.,]/g, '') || '0';

    colors.forEach(color => {
        // Set background color từ data-color-code
        const colorCode = color.getAttribute('data-color-code');
        if (colorCode) {
            color.style.backgroundColor = colorCode;
        }

        color.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();

            colors.forEach(c => c.classList.remove('active'));
            color.classList.add('active');

            const colorName = color.getAttribute('data-color');
            const colorId = color.getAttribute('data-color-id');
            let colorPriceNew = color.getAttribute('data-color-price-new');
            let colorPriceOld = color.getAttribute('data-color-price-old');
            
            console.log('Màu đã chọn:', colorName, 'ID:', colorId, 'Giá mới từ attribute:', colorPriceNew);
            
            // Lưu màu được chọn vào card để dùng khi thêm giỏ hàng
            card.setAttribute('data-selected-color-id', colorId);
            
            // Cập nhật giá: nếu màu có giá riêng thì dùng, nếu không thì dùng giá variant hiện tại
            if (colorPriceNew && colorPriceNew !== 'undefined' && colorPriceNew !== '') {
                updatePrice(card, colorPriceNew, colorPriceOld);
            } else {
                // Lấy giá từ variant hiện tại nếu màu không có giá riêng
                const activeVariant = card.querySelector('.capacity button.active');
                if (activeVariant) {
                    const variantPrice = activeVariant.getAttribute('data-price');
                    const variantOldPrice = activeVariant.getAttribute('data-old-price');
                    if (variantPrice) {
                        updatePrice(card, variantPrice, variantOldPrice);
                        console.log('Dùng giá variant:', variantPrice);
                    }
                }
            }
        });
    });
    
    // Khởi tạo giá cho màu đầu tiên
    const firstActiveColor = card.querySelector('.colors .color.active');
    if (firstActiveColor) {
        const firstColorPriceNew = firstActiveColor.getAttribute('data-color-price-new');
        const firstColorPriceOld = firstActiveColor.getAttribute('data-color-price-old');
        if (firstColorPriceNew && firstColorPriceNew !== 'undefined' && firstColorPriceNew !== '') {
            updatePrice(card, firstColorPriceNew, firstColorPriceOld);
        } else {
            // Lấy giá từ variant hiện tại
            const activeVariant = card.querySelector('.capacity button.active');
            if (activeVariant) {
                const variantPrice = activeVariant.getAttribute('data-price');
                const variantOldPrice = activeVariant.getAttribute('data-old-price');
                if (variantPrice) {
                    updatePrice(card, variantPrice, variantOldPrice);
                }
            }
        }
    }
}

function updatePrice(card, newPrice, oldPrice) {
    const priceNewElement = card.querySelector('.price-new');
    const priceOldElement = card.querySelector('.price-old');

    if (priceNewElement) {
        priceNewElement.textContent = formatPrice(newPrice) + '₫';
    }

    if (priceOldElement && oldPrice) {
        priceOldElement.textContent = formatPrice(oldPrice) + '₫';

        // Hiển thị/ẩn giá cũ dựa trên discount
        if (parseFloat(newPrice) < parseFloat(oldPrice)) {
            priceOldElement.style.display = 'inline';
        } else {
            priceOldElement.style.display = 'none';
        }
    }
}

function initCartSystem() {
    const cartButtons = document.querySelectorAll('.cart-btn');
    const cartBadge = document.getElementById('cart-badge');

    cartButtons.forEach(btn => {
        btn.onclick = function(e) {
            e.preventDefault();
            e.stopPropagation();

            const productCard = btn.closest('.product-card');
            const activeVariant = productCard.querySelector('.capacity button.active');

            if (!activeVariant) {
                alert("Vui lòng chọn phiên bản sản phẩm!");
                return;
            }

            // Kiểm tra xem có màu hay không
            const hasColors = productCard.querySelector('.colors .color');
            if (hasColors) {
                const activeColor = productCard.querySelector('.colors .color.active');
                if (!activeColor) {
                    alert("Vui lòng chọn màu sản phẩm!");
                    return;
                }
            }

            // LẤY vcId TỪNG CÁCH: đầu tiên từ variant_color_id, nếu không có thì từ data-id
            let vcId = null;
            
            // Cách 1: Lấy từ màu được chọn (nếu có màu)
            if (hasColors) {
                const activeColor = productCard.querySelector('.colors .color.active');
                vcId = activeColor.getAttribute('data-variant-color-id');
            }
            
            // Cách 2: Nếu không có màu hoặc không lấy được, lấy từ variant_color_id nếu có
            if (!vcId) {
                const variantId = activeVariant.getAttribute('data-id');
                const variantColorsData = productCard.querySelector('.variant-colors-data');
                if (variantColorsData) {
                    const colorGroup = variantColorsData.querySelector(`[data-variant-id="${variantId}"]`);
                    if (colorGroup) {
                        const firstColorItem = colorGroup.querySelector('.color-item');
                        if (firstColorItem) {
                            vcId = firstColorItem.getAttribute('data-variant-color-id');
                        }
                    }
                }
            }

            if (!vcId || vcId === "undefined") {
                console.error("ID biến thể không hợp lệ");
                alert("Lỗi: Không tìm thấy mã phiên bản sản phẩm.");
                return;
            }

            const productImg = productCard.querySelector('.product-img img');
            if (productImg) {
                flyToCart(productImg);
            }

            const header = document.getElementById('header');
            const contextPath = header ? header.getAttribute('data-context-path') : '';

            fetch(`${contextPath}/cart?action=add&vcId=${vcId}`)
                .then(response => {
                    // Nếu Server trả về 401 hoặc nội dung chứa HTML (do Filter ép)
                    if (response.status === 401 || response.redirected) {
                        alert("Vui lòng đăng nhập để tiếp tục!");
                        window.location.href = contextPath + "/login";
                        throw new Error("REDIRECT_TO_LOGIN");
                    }
                    return response.text();
                })
                .then(totalCount => {
                    // KIỂM TRA CHẶN LỖI VỠ FORM CUỐI CÙNG
                    if (totalCount.includes("<!DOCTYPE html>")) {
                        console.error("Vẫn bị trả về HTML thay vì số lượng!");
                        window.location.href = contextPath + "/login";
                        return;
                    }

                    const cartBadge = document.getElementById('cart-badge');
                    if (cartBadge && !isNaN(totalCount.trim())) {
                        cartBadge.textContent = totalCount.trim();
                    }
                })
                .catch(err => {
                    if (err.message !== "REDIRECT_TO_LOGIN") console.error(err);
                });

            // Hiệu ứng nút
            renderSuccessState(btn);
        }
    });
}

// HÀM QUAN TRỌNG: Hiệu ứng nút khi thêm thành công
function renderSuccessState(btn) {
    const originalHTML = btn.innerHTML;
    btn.innerHTML = '<i class="fa-solid fa-check"></i>';
    btn.style.backgroundColor = '#4caf50';
    btn.style.color = '#fff';
    btn.disabled = true;

    setTimeout(() => {
        btn.innerHTML = originalHTML;
        btn.style.backgroundColor = '';
        btn.style.color = '';
        btn.disabled = false;
    }, 1200);
}
function flyToCart(productImg) {
    const imgClone = productImg.cloneNode(true);
    const rect = productImg.getBoundingClientRect();
    console.log("Hàm bay đang chạy với ảnh:", productImg.src);
    // Lấy link ảnh gốc để đảm bảo clone hiện ra hình ảnh
    imgClone.src = productImg.src;

    imgClone.style.position = 'fixed';
    imgClone.style.left = rect.left + 'px';
    imgClone.style.top = rect.top + 'px';
    imgClone.style.width = rect.width + 'px';
    imgClone.style.height = rect.height + 'px';

    // ÉP BUỘC HIỂN THỊ
    imgClone.style.display = 'block';
    imgClone.style.visibility = 'visible';
    imgClone.style.zIndex = '2147483647';
    imgClone.style.transition = 'all 0.8s cubic-bezier(0.42, 0, 0.58, 1)';

    document.body.appendChild(imgClone);

    // 3. Tìm đích đến là badge số lượng (chính xác nhất)
    const cartBadge = document.getElementById('cart-badge');

    if (!cartBadge) {
        console.error("Không tìm thấy id='cart-badge' trên Header!");
        document.body.removeChild(imgClone);
        return;
    }

    // Lấy tọa độ tâm của giỏ hàng
    const badgeRect = cartBadge.getBoundingClientRect();
    const targetX = badgeRect.left + (badgeRect.width / 2) - 15;
    const targetY = badgeRect.top + (badgeRect.height / 2) - 15;

    // 4. Thực hiện bay
    setTimeout(() => {
        imgClone.style.left = targetX + 'px';
        imgClone.style.top = targetY + 'px';
        imgClone.style.width = '30px';
        imgClone.style.height = '30px';
        imgClone.style.opacity = '0.3';
    }, 20);

    // 5. Kết thúc và dọn dẹp
    setTimeout(() => {
        if (document.body.contains(imgClone)) {
            document.body.removeChild(imgClone);

            // Hiệu ứng "nảy" số lượng giỏ hàng
            cartBadge.style.transform = 'scale(1.5)';
            cartBadge.style.transition = 'transform 0.2s';
            setTimeout(() => {
                cartBadge.style.transform = 'scale(1)';
            }, 200);
        }
    }, 900);
}

// ===== HỆ THỐNG LOAD MORE =====
function initLoadMore() {
    // Hiển thị tất cả sản phẩm (tuân theo pagination backend - 12 sản phẩm/trang)
    const productCards = Array.from(document.querySelectorAll("#product-list .product-card"));
    productCards.forEach(card => {
        card.style.display = "flex";
        // Re-init events for all cards
        initCapacitySelection(card);
        initColorSelection(card);
    });
}

// ===== HELPER FUNCTIONS =====
function formatPrice(price) {
    const num = parseFloat(price);
    if (isNaN(num)) return '0';
    // Sử dụng 'de-DE' (tiếng Đức) để mặc định có dấu chấm phân cách hàng nghìn
    return num.toLocaleString('de-DE', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    });
}

function getBrandIdFromImage(brandElement) {
    const img = brandElement.querySelector('img');
    if (!img) return null;
    
    const alt = img.alt || '';
    const brandMap = {
        'iPhone': 1,
        'Samsung': 2,
        'Oppo': 3,
        'Vivo': 4,
        'Realme': 5,
        'Sony': 6,
        'Honor': 7
    };
    return brandMap[alt] || null;
}