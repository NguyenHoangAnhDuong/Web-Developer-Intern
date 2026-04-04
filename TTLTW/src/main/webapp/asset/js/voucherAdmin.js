document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("promoModal");
    const promoForm = document.getElementById("promoForm");
    const btnClose = document.getElementById("btnCloseModal");
    const btnOpen = document.getElementById("btnOpenModal");
    const rules = {
        promoCode: {
            el: () => document.getElementById("promoCode"),
            validate(v) {
                if (!v.trim()) return "Mã khuyến mãi không được để trống.";
                if (!/^[A-Z0-9_\-]{3,20}$/i.test(v.trim()))
                    return "Mã KM chỉ gồm chữ, số, dấu _ hoặc - (3–20 ký tự).";
                return null;
            },
        },
        promoType: {
            el: () => document.getElementById("promoType"),
            validate(v) {
                if (!v) return "Vui lòng chọn loại khuyến mãi.";
                return null;
            },
        },
        discountValue: {
            el: () => document.getElementById("discountValue"),
            validate(v) {
                if (v === "" || v === null) return "Mức giảm không được để trống.";
                const n = Number(v);
                if (isNaN(n) || n <= 0) return "Mức giảm phải là số lớn hơn 0.";
                const type = document.getElementById("promoType").value;
                if (type === "1" && n > 100) return "Giảm theo % không được vượt quá 100.";
                return null;
            },
        },
        maxDiscount: {
            el: () => document.getElementById("maxDiscount"),
            validate(v) {
                if (v === "" || v === null) return "Giảm tối đa không được để trống.";
                const n = Number(v);
                if (isNaN(n) || n < 0) return "Giảm tối đa phải là số không âm.";
                return null;
            },
        },
        minOrder: {
            el: () => document.getElementById("minOrder"),
            validate(v) {
                if (v === "" || v === null) return "Đơn tối thiểu không được để trống.";
                const n = Number(v);
                if (isNaN(n) || n < 0) return "Đơn tối thiểu phải là số không âm.";
                return null;
            },
        },
        quantity: {
            el: () => document.getElementById("quantity"),
            validate(v) {
                if (v === "" || v === null) return "Số lượng không được để trống.";
                const n = Number(v);
                if (!Number.isInteger(n) || n < 1) return "Số lượng phải là số nguyên ≥ 1.";
                return null;
            },
        },
        startDate: {
            el: () => document.getElementById("startDate"),
            validate(v) {
                if (!v) return "Ngày bắt đầu không được để trống.";
                return null;
            },
        },
        endDate: {
            el: () => document.getElementById("endDate"),
            validate(v) {
                if (!v) return "Ngày kết thúc không được để trống.";
                const start = document.getElementById("startDate").value;
                if (start && new Date(v) < new Date(start))
                    return "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.";
                return null;
            },
        },
    };
    (function injectCSS() {
        if (document.getElementById("_v-err-css")) return;
        const s = document.createElement("style");
        s.id = "_v-err-css";
        s.textContent = `
            .field-error {
                display: block;
                color: #e03535;
                font-size: 11.5px;
                font-style: italic;
                margin-top: 2px;
            }
            .input-error {
                border-color: #e03535 !important;
                background-color: #fff5f5 !important;
            }
        `;
        document.head.appendChild(s);
    })();
    function getErrorEl(field) {
        const el = field.el();
        if (!el) return null;
        let label = null;
        let sib = el.previousElementSibling;
        while (sib) {
            if (sib.tagName === "LABEL") { label = sib; break; }
            sib = sib.previousElementSibling;
        }
        if (!label) return null;

        let err = label.querySelector(".field-error");
        if (!err) {
            err = document.createElement("span");
            err.className = "field-error";
            label.appendChild(err);
        }
        return err;
    }
    function showError(key, msg) {
        const rule = rules[key];
        const el = rule.el();
        if (!el) return;
        el.classList.add("input-error");
        const errEl = getErrorEl(rule);
        if (errEl) errEl.textContent = msg;
    }
    function clearError(key) {
        const rule = rules[key];
        const el = rule.el();
        if (!el) return;
        el.classList.remove("input-error");
        const errEl = getErrorEl(rule);
        if (errEl) errEl.textContent = "";
    }
    function validateField(key) {
        const rule = rules[key];
        const el = rule.el();
        if (!el) return true;
        const msg = rule.validate(el.value);
        if (msg) { showError(key, msg); return false; }
        clearError(key);
        return true;
    }
    function validateAll() {
        let valid = true;
        for (const key of Object.keys(rules)) {
            if (!validateField(key)) valid = false;
        }
        return valid;
    }
    function clearAllErrors() {
        for (const key of Object.keys(rules)) clearError(key);
    }
    function attachRealtimeValidation() {
        document.getElementById("promoCode").addEventListener("input", function() {
            this.value = this.value.toUpperCase();
        });
        for (const key of Object.keys(rules)) {
            const el = rules[key].el();
            if (!el) continue;
            el.addEventListener("blur", () => validateField(key));
            el.addEventListener("input", () => {
                const msg = rules[key].validate(el.value);
                if (!msg) clearError(key);
                else showError(key, msg);
            });
            if (key === "promoType") {
                el.addEventListener("change", () => {
                    validateField("promoType");
                    validateField("discountValue");
                });
            }
            if (key === "startDate") {
                el.addEventListener("change", () => {
                    validateField("startDate");
                    validateField("endDate");
                });
            }
        }
    }
    function openModal() {
        modal.classList.add("show");
        modal.style.display = "flex";
    }
    function closeModal() {
        modal.classList.remove("show");
        modal.style.display = "none";
        clearAllErrors();
    }
    if (btnOpen) {
        btnOpen.addEventListener("click", function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (!modal || !promoForm) return;
            promoForm.reset();
            promoForm.querySelectorAll("input, select").forEach((el) => {
                el.disabled = false;
                el.readOnly = false;
            });
            document.getElementById("formAction").value = "addVoucher";
            document.getElementById("editId").value = "";
            const modalTitle = document.querySelector("#promoModal h3");
            if (modalTitle) modalTitle.innerText = "Thêm khuyến mãi mới";
            clearAllErrors();
            openModal();
        });
    }
    if (btnClose) btnClose.addEventListener("click", closeModal);
    window.addEventListener("click", (e) => { if (e.target === modal) closeModal(); });
    const modalContent = document.querySelector(".modal-content");
    if (modalContent) modalContent.addEventListener("click", (e) => e.stopPropagation());
    window.editRow = function (btn) {
        if (!btn || !modal) return;
        const row = btn.closest("tr");
        if (!row) return;
        promoForm.querySelectorAll("input, select").forEach((el) => {
            el.disabled = false;
            el.readOnly = false;
        });
        document.getElementById("formAction").value = "update";
        document.getElementById("editId").value = btn.dataset.id;
        document.getElementById("promoCode").value = row.children[0].innerText.trim();
        const typeText = row.children[1].innerText.trim();
        document.getElementById("promoType").value =
            typeText === "Phần trăm" ? "1" : typeText === "Tiền mặt" ? "2" : "3";
        const discountText = row.children[2].innerText.trim().replace(/[^\d]/g, "");
        document.getElementById("discountValue").value = discountText || "";
        const maxDiscountText = row.children[3].innerText.replace(/[^\d]/g, "");
        document.getElementById("maxDiscount").value = maxDiscountText || "";
        const minOrderText = row.children[4].innerText.replace(/[^\d]/g, "");
        document.getElementById("minOrder").value = minOrderText || "";
        document.getElementById("quantity").value =
            parseInt(row.children[5].innerText.trim()) || "";
        const startDateCell = row.children[6];
        document.getElementById("startDate").value =
            startDateCell.getAttribute("data-date") || startDateCell.innerText.trim();
        const endDateCell = row.children[7];
        document.getElementById("endDate").value =
            endDateCell.getAttribute("data-date") || endDateCell.innerText.trim();
        const modalTitle = document.querySelector("#promoModal h3");
        if (modalTitle) modalTitle.innerText = "Cập nhật khuyến mãi";
        clearAllErrors();
        openModal();
    };
    if (promoForm) {
        promoForm.addEventListener("submit", function (e) {
            const action = document.getElementById("formAction").value;
            if (!action) {
                e.preventDefault();
                showToast("Lỗi: Action không được xác định!", "error");
                return;
            }
            const isValid = validateAll();
            if (!isValid) {
                e.preventDefault();
                showToast("Vui lòng kiểm tra lại thông tin trong form!", "error");
                const firstErr = promoForm.querySelector(".input-error");
                if (firstErr) firstErr.scrollIntoView({ behavior: "smooth", block: "center" });
            }
        });
    }
    attachRealtimeValidation();

    document.querySelectorAll(".btn-toggle").forEach(btn => {
        btn.addEventListener("click", function(e) {
            const row = this.closest("tr");
            const isInactive = row.querySelector(".status.inactive") !== null;
            const endDateStr = row.children[7].getAttribute("data-date");

            if (isInactive && endDateStr) {
                const endDate = new Date(endDateStr);
                if (endDate < new Date()) {
                    e.preventDefault();
                    showToast(
                        "Voucher đã hết hạn, vui lòng cập nhật ngày kết thúc trước khi bật lại.",
                        "error"
                    );
                }
            }
        });
    });
});