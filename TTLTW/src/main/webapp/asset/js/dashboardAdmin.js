const dashAddPromo = document.getElementById("dashAddPromo");
if (dashAddPromo) {
    dashAddPromo.addEventListener("click", function () {
        window.location.href = "vouchersAdmin.jsp?addPromo=true";
    });
}

window.addEventListener('scroll', function () {
    const topbar = document.querySelector('.topbar');
    if (!topbar) return;
    if (window.scrollY > 50) {
        topbar.classList.add('transparent');
    } else {
        topbar.classList.remove('transparent');
    }
});

// Vẽ biểu đồ doanh thu theo ngày
const salesLineCanvas = document.getElementById('salesLine');
if (salesLineCanvas) {
    const ctx = salesLineCanvas.getContext('2d');
    const labels = revenueByDaysData.map(item => item.date);
    const data = revenueByDaysData.map(item => parseFloat(item.revenue));
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh thu', data: data, borderColor: 'rgb(75, 192, 192)', tension: 0.1
            }]
        },
        options: {responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// Vẽ biểu đồ doanh thu theo danh mục
const categoryPieCanvas = document.getElementById('categoryPie');
if (categoryPieCanvas) {
    const ctx = categoryPieCanvas.getContext('2d');
    const labels = revenueByCategoryData.map(item => item.category);
    const data = revenueByCategoryData.map(item => parseFloat(item.revenue));
    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh thu',
                data: data,
                backgroundColor: ['rgb(255, 99, 132)', 'rgb(54, 162, 235)', 'rgb(255, 205, 86)', 'rgb(75, 192, 192)', 'rgb(153, 102, 255)'
                ]
            }]
        },
        options: {
            responsive: true
        }
    });
}
// Vẽ biểu đồ sản phẩm bán chạy
const topProductsCanvas = document.getElementById('topProductsBar');
if (topProductsCanvas) {
    const ctx = topProductsCanvas.getContext('2d');
    const labels = topProductsData.map(item => item.product);
    const data = topProductsData.map(item => parseInt(item.sold));
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Số lượng bán', data: data,
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
                borderColor: 'rgb(54, 162, 235)', borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}
