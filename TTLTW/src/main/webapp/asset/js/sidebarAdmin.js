toggleSidebar();
activeSidebar();
// đóng mở menu
function toggleSidebar() {
    const sidebar = document.getElementById("sidebar");
    const sidebarToggle = document.querySelector(".sidebar-toggle");
    sidebarToggle.addEventListener("click", function() {
        sidebar.classList.toggle("collapsed");
        toggleHeader();
    });
}
//co dãn theo toggle
function toggleHeader() {
    const sidebar = document.getElementById("sidebar");
    const topbar = document.querySelector(".topbar");
    const container = document.querySelector(".container");
    if (sidebar.classList.contains("collapsed")) {
        sidebar.style.width = "60px";
        if (container) container.style.marginLeft = "175px";
        if (topbar) {
            topbar.style.width = "calc(100% - 60px)";
            topbar.style.left = "60px";
        }
    } else {
        sidebar.style.width = "250px";
        if (container) container.style.marginLeft = "250px";
        if (topbar) {
            topbar.style.width = "calc(100% - 250px)";
            topbar.style.left = "250px";
        }

    }
}
function activeSidebar() {
    const navLinks = document.querySelectorAll(".nav-link");
    const currentPath = window.location.pathname;
    let foundActive = false;
    navLinks.forEach(link => {
        const href = link.getAttribute("href");
        if (!href) return;
        // Xóa active cũ
        link.classList.remove("active");
        // Nếu path hiện tại chứa href
        if (currentPath.startsWith(href)) {
            link.classList.add("active");
            foundActive = true;
        }
    });
    if (!foundActive) {
        const dashboardLink = document.querySelector(
            '.nav-link[data-target="dashboard"]'
        );
        if (dashboardLink) dashboardLink.classList.add("active");
    }
}




