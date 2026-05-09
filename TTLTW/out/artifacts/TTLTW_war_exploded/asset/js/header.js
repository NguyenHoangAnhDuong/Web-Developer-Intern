document.addEventListener("DOMContentLoaded", () => {
    initHeaderSearch();
    initUserDropdown();
    initMegaMenu();
    initLoginRedirect();
    setActiveMenuItem();
    initSearchSuggest();
});

function initHeaderSearch() {
    const searchBtn = document.getElementById("btn-search");
    const searchInput = document.getElementById("header-search");
    if (!searchBtn || !searchInput) return;

    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('search');
    if (searchQuery) {
        searchInput.value = searchQuery;
        searchInput.classList.add("show");
    }
    searchBtn.onclick = (e) => {
        e.preventDefault();
        const query = searchInput.value.trim();
        if (query) {
            performSearch(query);
        } else {
            const show = !searchInput.classList.contains("show");
            searchInput.classList.toggle("show", show);
            if (show) searchInput.focus();
        }
    };
    searchInput.onblur = () => {
        if (searchInput.value.trim() === "") {
            searchInput.classList.remove("show");
        }
    };
    // Thêm logic tìm kiếm khi nhấn Enter
    searchInput.onkeydown = (e) => {
        if (e.key === "Enter") {
            performSearch(searchInput.value.trim());
        }
    };
}
function performSearch(query) {
    if (!query) return;

    const contextPath = document.getElementById("header").getAttribute("data-context-path") || "";
    const currentPath = window.location.pathname;
    let targetUrl;
    // Nếu đang ở trang accessory, giữ nguyên
    if (currentPath.includes("listproduct_accessory")) {
        targetUrl = contextPath + "/listproduct_accessory?search=" + encodeURIComponent(query);
    } 
    // Nếu đang ở trang phone, giữ nguyên
    else if (currentPath.includes("listproduct")) {
        targetUrl = contextPath + "/listproduct?search=" + encodeURIComponent(query);
    } 
    // Từ trang khác: phân loại theo từ khóa
    else {
        // Danh sách từ khóa điện thoại
        const phoneKeywords = ['iphone', 'samsung', 'oppo', 'xiaomi', 'vivo', 'realme', 
                               'nokia', 'phone', 'điện thoại', 'smartphone'];
        
        const queryLower = query.toLowerCase();
        const isPhoneQuery = phoneKeywords.some(keyword => queryLower.includes(keyword));
        
        targetUrl = isPhoneQuery 
            ? contextPath + "/listproduct?search=" + encodeURIComponent(query)
            : contextPath + "/listproduct_accessory?search=" + encodeURIComponent(query);
    }
    window.location.href = targetUrl;
}

function initUserDropdown() {
    const userArea = document.getElementById("user-area");
    const profileBtn = document.getElementById("user-profile");
    const usernameSpan = document.getElementById("header-username");
    if (!userArea || !profileBtn || !usernameSpan) return;
    // Kiểm tra xem user đã đăng nhập chưa (dựa vào nội dung span)
    const isLoggedIn = usernameSpan.textContent.trim() !== "Đăng nhập";
    if (isLoggedIn) {
        // Đã đăng nhập: toggle dropdown
        profileBtn.onclick = (e) => {
            e.preventDefault();
            userArea.classList.toggle("open");
        };
        // Click outside để đóng dropdown
        document.addEventListener("click", (e) => {
            if (!userArea.contains(e.target)) {
                userArea.classList.remove("open");
            }
        });
    } else {
        // Chưa đăng nhập: chuyển đến trang login
        profileBtn.onclick = (e) => {
            e.preventDefault();
            window.location.href = getContextPath() + "/login";
        };
    }
}
function initLoginRedirect() {
    const usernameSpan = document.getElementById("header-username");
    if (!usernameSpan) return;
    // Nếu nội dung là "Đăng nhập", cho phép click
    if (usernameSpan.textContent.trim() === "Đăng nhập") {
        usernameSpan.style.cursor = "pointer";
        usernameSpan.onclick = (e) => {
            e.preventDefault();
            window.location.href = getContextPath() + "/login";
        };
    }
}
function initMegaMenu() {
    const accessoryItem = document.getElementById("nav-accessory-item");
    const megaAccessory = document.getElementById("mega-accessory");
    if (!accessoryItem || !megaAccessory) return;
    accessoryItem.addEventListener("mouseenter", () => {
        if (window.innerWidth > 768) {
            megaAccessory.style.display = "block";
        }
    });
    accessoryItem.addEventListener("mouseleave", () => {
        if (window.innerWidth > 768) {
            megaAccessory.style.display = "none";
        }
    });
    const accessoryLink = document.getElementById("nav-accessory");
    if (accessoryLink) {
        accessoryLink.onclick = (e) => {
            if (window.innerWidth <= 768) {
                e.preventDefault();
                const isOpen = accessoryItem.classList.toggle("open");
                megaAccessory.style.display = isOpen ? "block" : "none";
            }
        };
    }
    window.addEventListener("resize", () => {
        if (window.innerWidth > 768) {
            megaAccessory.style.display = "";
            accessoryItem.classList.remove("open");
        }
    });
}
function setActiveMenuItem() {
    const currentPath = window.location.pathname;
    const navHome = document.getElementById("nav-home");
    const navPhone = document.getElementById("nav-phone");
    const navAccessory = document.getElementById("nav-accessory");
    const navAccessoryItem = document.getElementById("nav-accessory-item");

    if (navHome) navHome.classList.remove("active");
    if (navPhone) navPhone.classList.remove("active");
    if (navAccessory) navAccessory.classList.remove("active");
    if (navAccessoryItem) navAccessoryItem.classList.remove("active");

    if (currentPath.includes("listproduct_accessory")) {
        if (navAccessory) navAccessory.classList.add("active");
        if (navAccessoryItem) navAccessoryItem.classList.add("active");
    } else if (currentPath.includes("listproduct")) {
        if (navPhone) navPhone.classList.add("active");
    } else if (currentPath.includes("home") || currentPath.endsWith("/")) {
        if (navHome) navHome.classList.add("active");
    }
}

function getContextPath() {
    const header = document.querySelector('header');
    if (header && header.getAttribute('data-context-path')) {
        return header.getAttribute('data-context-path');
    }
    const pathArray = window.location.pathname.split('/');
    return pathArray.length > 1 && pathArray[1] ? '/' + pathArray[1] : '';
}

window.getContextPath = getContextPath;

function initSearchSuggest() {
    const input = document.getElementById("header-search");
    const suggestBox = document.getElementById("search-suggest");

    if (!input || !suggestBox) return;

    input.addEventListener("input", async function () {
        const value = this.value.trim();

        // rỗng thì ẩn
        if (value === "") {
            suggestBox.style.display = "none";
            return;
        }

        try {
            const contextPath = getContextPath();

            const res = await fetch(
                `${contextPath}/search-suggest?q=${encodeURIComponent(value)}`
            );

            const data = await res.json();

            suggestBox.innerHTML = "";

            data.forEach(item => {
                const div = document.createElement("div");

                // highlight chữ
                div.innerHTML = item.replace(
                    new RegExp(value, "gi"),
                    match => `<b>${match}</b>`
                );

                div.onclick = () => {
                    input.value = item;
                    suggestBox.style.display = "none";
                };

                suggestBox.appendChild(div);
            });

            suggestBox.style.display = "block";

        } catch (err) {
            console.error("Lỗi search suggest:", err);
        }
    });


    document.addEventListener("click", (e) => {
        if (!document.querySelector(".search-item").contains(e.target)) {
            suggestBox.style.display = "none";
        }
    });
}