let variantCounter = 0;

function showPhone() {
    document.getElementById("phoneForm").style.display = "block";
    document.getElementById("partForm").style.display = "none";
}

function showPart() {
    document.getElementById("phoneForm").style.display = "none";
    document.getElementById("partForm").style.display = "block";
}
// Cập nhập thêm tham số data để điền giá trị
function addTech(targetId, data = null) {
    let container = document.getElementById(targetId);
    let tpl = document.getElementById("techTpl").content.cloneNode(true);

    if (data) {
        tpl.querySelector('input[name="techName[]"]').value = data.tech_name;
        tpl.querySelector('input[name="techValue[]"]').value = data.tech_value;
        tpl.querySelector('input[name="techPriority[]"]').value = data.priority;
    }

    container.appendChild(tpl);
}
function addColor(btn) {
    const variantDiv = btn.closest('.variant');
    const colorsBox = variantDiv.querySelector('.colors');

    const variantIndex = variantDiv.dataset.variantIndex;
    const colorIndex = colorsBox.children.length;

    const tpl = document.getElementById('colorTpl');
    const clone = tpl.content.cloneNode(true);
    const colorDiv = clone.querySelector('.color');

    // gắn variant index
    colorDiv.querySelector('.variant-index-input').value = variantIndex;

    // gắn name cho file input (NHIỀU ẢNH)
    const fileInput = colorDiv.querySelector('.color-image-input');
    fileInput.name = `colorImages_${variantIndex}_${colorIndex}`;

    colorsBox.appendChild(clone);
}


function addVariant(targetId, data = null) {
    let tplId = (targetId === 'phoneVariant')
        ? "phoneVariantTpl"
        : "partVariantTpl";

    const templateElement = document.getElementById(tplId);
    if (!templateElement) return;

    let tpl = templateElement.content.cloneNode(true);
    let variantDiv = tpl.querySelector('.variant');

    //   GÁN VARIANT INDEX CỐ ĐỊNH
    const currentVariantIndex = variantCounter;
    variantDiv.dataset.variantIndex = currentVariantIndex;
    variantCounter++;

    if (data) {
        variantDiv.querySelector('input[name="variantName[]"]').value = data.variant_name;
        variantDiv.querySelector('input[name="basePrice[]"]').value = data.base_price;
    }

    document.getElementById(targetId).appendChild(variantDiv);

    // đổ màu khi edit
    if (data && data.colors) {
        data.colors.forEach((c, idx) => {
            addColorManual(variantDiv, c, idx);
        });
    }
}


function removeBlock(btn) {
    btn.parentElement.remove();
}
function toggleBrand(select) {
    const row = select.closest('.brand-row');
    if (!row) return;

    const input = row.querySelector('input[name="customBrand"]');
    if (!input) return;

    if (select.value === 'custom') {
        input.style.display = 'inline-block';
        input.required = true;
    } else {
        input.style.display = 'none';
        input.value = '';
        input.required = false;
    }
}
function toggleColor(select) {
    const row = select.closest('.color-row');
    if (!row) return;

    const input = row.querySelector('input[name="customColor[]"]');
    if (!input) return;

    if (select.value === 'custom') {
        input.style.display = 'inline-block';
        input.required = true;
    } else {
        input.style.display = 'none';
        input.value = '';
        input.required = false;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Biến productData này được định nghĩa trực tiếp trong file JSP (xem hướng dẫn ở dưới)
    if (typeof productData !== 'undefined' && productData !== null) {
        console.log("Đang ở chế độ Edit, khởi tạo dữ liệu...");

        const categoryId = parseInt(productData.category_id);
        const formId = categoryId === 1 ? 'phoneForm' : 'partForm';
        const techContainerId = categoryId === 1 ? 'phoneTech' : 'partTech';
        const variantContainerId = categoryId === 1 ? 'phoneVariant' : 'partVariant';

        if (categoryId === 1) showPhone(); else showPart();

        const form = document.getElementById(formId);
        form.querySelector('input[name="productName"]').value = productData.product_name;
        form.querySelector('textarea[name="description"]').value = productData.description || "";

        if (productData.techs) {
            productData.techs.forEach(t => addTech(techContainerId, t));
        }

        if (productData.variants) {
            productData.variants.forEach(v => addVariant(variantContainerId, v));
        }

        const typeSelect = document.querySelector('.type-select');
        if (typeSelect) typeSelect.style.display = 'none';

        form.querySelector('button[type="submit"]').innerText = "Cập nhật sản phẩm";
    }
});


