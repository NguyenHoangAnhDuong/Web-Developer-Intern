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
                label: 'Doanh thu (VNĐ)',
                data: data,
                borderColor: 'rgb(75, 192, 192)',
                backgroundColor: 'rgba(75, 192, 192, 0.1)',
                tension: 0.4,
                fill: true,
                pointBackgroundColor: 'rgb(75, 192, 192)',
                pointBorderColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Doanh thu: ' + new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', notation: 'compact' }).format(value);
                        }
                    }
                },
                x: {
                    ticks: {
                        maxTicksLimit: 10
                    }
                }
            },
            interaction: {
                intersect: false,
                mode: 'index'
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
                backgroundColor: [
                    'rgba(255, 99, 132, 0.8)',
                    'rgba(54, 162, 235, 0.8)',
                    'rgba(255, 205, 86, 0.8)',
                    'rgba(75, 192, 192, 0.8)',
                    'rgba(153, 102, 255, 0.8)',
                    'rgba(255, 159, 64, 0.8)',
                    'rgba(201, 203, 207, 0.8)'
                ],
                borderColor: [
                    'rgb(255, 99, 132)',
                    'rgb(54, 162, 235)',
                    'rgb(255, 205, 86)',
                    'rgb(75, 192, 192)',
                    'rgb(153, 102, 255)',
                    'rgb(255, 159, 64)',
                    'rgb(201, 203, 207)'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        boxWidth: 12,
                        padding: 15
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return label + ': ' + new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value) + ' (' + percentage + '%)';
                        }
                    }
                }
            }
        }
    });
}
// Vẽ biểu đồ sản phẩm bán chạy
const topProductsCanvas = document.getElementById('topProductsBar');
if (topProductsCanvas) {
    const ctx = topProductsCanvas.getContext('2d');
    const labels = topProductsData.map(item => item.product.length > 20 ? item.product.substring(0, 20) + '...' : item.product);
    const data = topProductsData.map(item => parseInt(item.sold));
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Số lượng bán',
                data: data,
                backgroundColor: 'rgba(54, 162, 235, 0.6)',
                borderColor: 'rgb(54, 162, 235)',
                borderWidth: 1,
                borderRadius: 4,
                borderSkipped: false,
                hoverBackgroundColor: 'rgba(54, 162, 235, 0.8)'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        title: function(context) {
                            const index = context[0].dataIndex;
                            return topProductsData[index].product;
                        },
                        label: function(context) {
                            return 'Đã bán: ' + context.parsed.y + ' sản phẩm';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                },
                x: {
                    ticks: {
                        maxRotation: 45,
                        minRotation: 45
                    }
                }
            }
        }
    });
}
