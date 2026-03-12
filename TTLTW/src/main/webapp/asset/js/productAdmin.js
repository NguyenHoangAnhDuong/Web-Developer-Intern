document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".ajax-toggle").forEach(btn => {
        btn.addEventListener("click", function () {
            const id = this.dataset.id;
            const icon = this.querySelector("i");
            fetch(`${contextPath}/admin/products`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: `action=toggle&id=${id}`
            })
                .then(res => {
                    if (!res.ok) throw new Error("Toggle failed");
                    // đổi icon ngay trên UI
                    if (icon.classList.contains("fa-eye")) {
                        icon.classList.remove("fa-eye");
                        icon.classList.add("fa-eye-slash");
                        icon.style.color = "#e74c3c";
                    } else {
                        icon.classList.remove("fa-eye-slash");
                        icon.classList.add("fa-eye");
                        icon.style.color = "#2ecc71";
                    }
                })
                .catch(err => {
                    alert("Không thể đổi trạng thái!");
                    console.error(err);
                });
        });
    });
});
