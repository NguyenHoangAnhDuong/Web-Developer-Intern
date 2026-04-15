document.addEventListener("DOMContentLoaded", () => {
    const DATA = window.PRODUCT_DATA || {};
    const discount = DATA.discount || 0;
    const currentPriceEl = document.querySelector(".current-price");
    const oldPriceEl = document.querySelector(".old-price");
    const discountEl = document.querySelector(".discount");

    const versionBtns = document.querySelectorAll(".version");
    const colorItems = document.querySelectorAll(".color-item");
    const btnCart = document.querySelector(".btn-cart");
    const btnBuy = document.querySelector(".btn-buy");

    let selectedVariantId = null;
    let selectedColorId = null;


    function formatPrice(p) {
        return Number(p).toLocaleString("vi-VN");
    }

    function updatePrice(basePrice) {
        if (!currentPriceEl || !basePrice) return;

        const discounted = basePrice * (100 - discount) / 100;

        currentPriceEl.textContent = formatPrice(discounted) + "₫";

        if (discount > 0) {
            oldPriceEl.textContent = formatPrice(basePrice) + "₫";
            oldPriceEl.style.display = "inline";
            discountEl.textContent = `-${discount}%`;
            discountEl.style.display = "inline";
        } else {
            oldPriceEl.style.display = "none";
            discountEl.style.display = "none";
        }
    }
    function initDefault() {
        const activeVariant = document.querySelector(".version.active");
        const activeColor = document.querySelector(".color-item.active");

        if (activeVariant) selectedVariantId = activeVariant.dataset.variantId;
        if (activeColor) {
            selectedColorId = activeColor.dataset.colorId;
            updatePrice(Number(activeColor.dataset.price));
        }
    }
    versionBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            versionBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            selectedVariantId = btn.dataset.variantId;

            colorItems.forEach(color => {
                const key = `${selectedVariantId}_${color.dataset.colorId}`;
                const vc = DATA.variantColorPrices[key];
                const vcId = DATA.variantColorIds[key];

                if (vc) color.dataset.price = vc.price;
                if (vcId) color.dataset.variantColorId = vcId;
            });

            const activeColor = document.querySelector(".color-item.active");
            if (activeColor) updatePrice(Number(activeColor.dataset.price));
        });
    });

    colorItems.forEach(item => {
        item.addEventListener("click", () => {
            colorItems.forEach(c => c.classList.remove("active"));
            item.classList.add("active");

            selectedColorId = item.dataset.colorId;
            updatePrice(Number(item.dataset.price));
        });
    });
    function getContextPath() {
        const header = document.getElementById("header");
        return header ? header.dataset.contextPath : "";
    }

    btnCart?.addEventListener("click", e => {
        e.preventDefault();

        const activeColor = document.querySelector(".color-item.active");
        if (!activeColor) return showToast("Vui lòng chọn phiên bản và màu","error");

        const vcId = activeColor.dataset.variantColorId;
        if (!vcId) return showToast("Lỗi sản phẩm", "error");
        const productImg = document.querySelector(".img-feature");
        fetch(`${getContextPath()}/cart?action=add&vcId=${vcId}`)
            .then(r => {
                if (r.redirected && r.url.includes("/login")) {
                    window.location.href = r.url;
                    return null;
                }
                return r.text();
            })
            .then(result => {
                if (!result) return;
                if (result === "STOCK_EXCEEDED") {
                    showToast("Sản phẩm đã hết hàng!", "error");
                    return;
                }
                if (productImg) flyToCart(productImg);
                showToast("Đã thêm vào giỏ hàng!", "success");
                renderSuccess(btnCart);
            })
            .catch(() => showToast("Có lỗi xảy ra, vui lòng thử lại", "error"));
    });

    btnBuy?.addEventListener("click", e => {
        e.preventDefault();

        const activeColor = document.querySelector(".color-item.active");
        if (!activeColor) return showToast("Vui lòng chọn phiên bản và màu", "error");

        const vcId = activeColor.dataset.variantColorId;
        fetch(`${getContextPath()}/cart?action=add&vcId=${vcId}`)
            .then(r => {
                if (r.redirected && r.url.includes("/login")) {
                    window.location.href = r.url;
                    return null;
                }
                return r.text();
            })
            .then(result => {
                if (!result) return;
                if (result === "STOCK_EXCEEDED") {
                    showToast("Sản phẩm đã hết hàng!", "error");
                    return;
                }
                window.location.href =
                    `${getContextPath()}/cart?action=checkout&selectedIds=${vcId}`;
            })
            .catch(() => showToast("Có lỗi xảy ra, vui lòng thử lại", "error"));
    });

    function renderSuccess(btn) {
        const html = btn.innerHTML;
        btn.innerHTML = "✔";
        btn.disabled = true;
        setTimeout(() => {
            btn.innerHTML = html;
            btn.disabled = false;
        }, 1000);
    }
    initDefault();
});
