document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("bankForm");
    const bankSelect = document.getElementById("bankSelect");
    const accountNumber = document.getElementById("accountNumber");
    const ownerName = document.getElementById("ownerName");
    const btnBack = document.getElementById("btnBack");
    const btnSubmit = document.getElementById("btnSubmit");

    let isEditMode = false;
    const originalValues = {
        bank: bankSelect.value || "",
        accountNumber: accountNumber.value || "",
        ownerName: ownerName.value || ""
    };
    // Khởi tạo: disable fields
    function disableFields() {
        bankSelect.disabled = true;
        accountNumber.disabled = true;
        ownerName.disabled = true;
        isEditMode = false;
        btnBack.textContent = "Sửa";
    }
    function enableFields() {
        bankSelect.disabled = false;
        accountNumber.disabled = false;
        ownerName.disabled = false;
        isEditMode = true;
        btnBack.textContent = "Hủy";
    }

    disableFields();

    // Nút Sửa/Hủy
    btnBack.addEventListener("click", (e) => {
        e.preventDefault();
        if (!isEditMode) {
            // Lưu giá trị hiện tại
            originalValues.bank = bankSelect.value;
            originalValues.accountNumber = accountNumber.value;
            originalValues.ownerName = ownerName.value;
            enableFields();
        } else {
            // Khôi phục giá trị cũ
            if (confirm("Hủy thay đổi?")) {
                bankSelect.value = originalValues.bank;
                accountNumber.value = originalValues.accountNumber;
                ownerName.value = originalValues.ownerName;
                disableFields();
            }
        }
    });

    // Validate input
    accountNumber.addEventListener("input", () => {
        if (isEditMode) {
            accountNumber.value = accountNumber.value.replace(/\D/g, "");
        }
    });

    ownerName.addEventListener("input", () => {
        if (isEditMode) {
            ownerName.value = ownerName.value.toUpperCase().replace(/[^A-Z ]/g, "");
        }
    });

    // SUBMIT FORM
    form.addEventListener("submit", (e) => {
        e.preventDefault();

        if (!isEditMode) {
            alert("Vui lòng nhấn nút 'Sửa' để chỉnh sửa!");
            return;
        }
        // Validate
        const bank = bankSelect.value.trim();
        const accNum = accountNumber.value.trim();
        const accName = ownerName.value.trim();

        if (!bank) {
            alert("Vui lòng chọn ngân hàng!");
            return;
        }
        if (!accNum || accNum.length < 6) {
            alert("Số tài khoản phải có ít nhất 6 chữ số!");
            return;
        }
        if (!accName) {
            alert("Vui lòng nhập tên chủ tài khoản!");
            return;
        }
        const formData = new URLSearchParams();
        formData.append("bank_name", bank);
        formData.append("account_number", accNum);
        formData.append("account_name", accName);
        // Gửi request
        fetch(form.action, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData.toString()
        })
            .then(response => {
                console.log("Response received:", response.status);
                if (response.ok || response.redirected) {
                    console.log("Success! Redirecting...");
                    window.location.href = form.action;
                } else {
                    return response.text().then(text => {
                        console.error("Error response:", text);
                        alert("Lỗi khi lưu dữ liệu!");
                    });
                }
            })
            .catch(error => {
                console.error("Fetch error:", error);
                alert("Không thể kết nối server!");
            });
    });
    if (menuAccountMain && accountSubmenu) {
        accountSubmenu.classList.add("open");
        menuAccountMain.addEventListener("click", (e) => {
            e.preventDefault();
            accountSubmenu.classList.toggle("open");
        });
    }

});
