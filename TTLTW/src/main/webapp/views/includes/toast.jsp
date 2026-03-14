<style>
    .toast {
        position: fixed;
        top: 20px;
        right: 30px;
        min-width: 260px;
        padding: 12px 18px;
        border-radius: 8px;
        color: #fff;
        font-size: 14px;
        font-weight: 500;
        opacity: 0;
        transform: translateY(-20px);
        transition: all 0.4s ease;
        z-index: 9999999;
        box-shadow: 0 8px 24px rgba(0,0,0,0.15);
    }

    .toast.show {
        opacity: 1;
        transform: translateY(0);
    }

    .toast.success {
        background: #22c55e;
    }

    .toast.error {
        background: #ef4444;
    }
</style>
<%
    String toastMessage = (String) session.getAttribute("toastMessage");
    String toastType = (String) session.getAttribute("toastType"); // success hoặc error

    if (toastMessage != null) {
%>



<div id="toast" class="toast <%= toastType %>">
    <%= toastMessage %>
</div>

<script>
    setTimeout(() => {
        document.getElementById("toast")?.classList.add("show");
    }, 100);

    setTimeout(() => {
        document.getElementById("toast")?.classList.remove("show");
    }, 3500);
</script>

<%
        session.removeAttribute("toastMessage");
        session.removeAttribute("toastType");
    }
%>
<script>
    function showToast(message, type = "success") {

        const oldToast = document.getElementById("toast");
        if (oldToast) oldToast.remove();

        const toast = document.createElement("div");
        toast.id = "toast";
        toast.className = "toast " + type;
        toast.innerText = message;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.classList.add("show");
        }, 100);

        setTimeout(() => {
            toast.classList.remove("show");
            setTimeout(()=>toast.remove(),400);
        }, 3500);
    }
</script>