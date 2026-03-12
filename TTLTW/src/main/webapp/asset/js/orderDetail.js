document.addEventListener("DOMContentLoaded", function () {
    // Hàm mua lại được gọi từ onclick trong JSP
    window.handleReorder = function() {
        const contextPath = document.body.getAttribute('data-context-path') || '/WebDynamic_war_exploded';
        window.location.href = contextPath + '/cart';
    }
});
