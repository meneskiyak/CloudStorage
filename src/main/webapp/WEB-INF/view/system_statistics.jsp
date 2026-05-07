<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Admin | Sistem İstatistikleri</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body class="bg-light">
<nav class="navbar navbar-dark bg-primary mb-4">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/admin/dashboard">Admin Paneli</a>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline-light btn-sm">Geri Dön</a>
    </div>
</nav>

<div class="container">
    <div class="row mb-4">
        <div class="col-md-12">
            <h2>Sistem Genel İstatistikleri</h2>
            <p class="text-muted">Tüm kullanıcıların toplam veri kullanımı ve kapasite durumu.</p>
        </div>
    </div>

    <div class="row">
        <!-- Özet Kartları -->
        <div class="col-md-4">
            <div class="card shadow-sm border-0 mb-4 bg-white">
                <div class="card-body text-center">
                    <h6 class="text-muted text-uppercase small">Toplam Kullanıcı</h6>
                    <h3 class="fw-bold">${stats.userCount}</h3>
                </div>
            </div>
            <div class="card shadow-sm border-0 mb-4 bg-white">
                <div class="card-body text-center">
                    <h6 class="text-muted text-uppercase small">Toplam Kapasite</h6>
                    <h3 class="fw-bold text-primary">
                        <fmt:formatNumber value="${stats.totalLimit / 1073741824}" maxFractionDigits="1"/> GB
                    </h3>
                </div>
            </div>
            <div class="card shadow-sm border-0 mb-4 bg-white">
                <div class="card-body text-center">
                    <h6 class="text-muted text-uppercase small">Kullanılan Alan</h6>
                    <h3 class="fw-bold text-danger">
                        <fmt:formatNumber value="${stats.totalUsed / (1024*1024)}" maxFractionDigits="1"/> MB
                    </h3>
                </div>
            </div>
        </div>

        <!-- Pasta Grafiği -->
        <div class="col-md-8">
            <div class="card shadow-sm border-0 bg-white p-4">
                <h5 class="text-center mb-4">Depolama Alanı Dağılımı (Sistem Geneli)</h5>
                <div style="max-width: 450px; margin: 0 auto;">
                    <canvas id="storageChart"></canvas>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    const ctx = document.getElementById('storageChart').getContext('2d');
    const storageChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ['Kullanılan Alan', 'Boş Alan'],
            datasets: [{
                data: [
                    ${stats.totalUsed}, 
                    ${stats.totalFree}
                ],
                backgroundColor: [
                    'rgba(255, 99, 132, 0.7)',
                    'rgba(75, 192, 192, 0.7)'
                ],
                borderColor: [
                    'rgba(255, 99, 132, 1)',
                    'rgba(75, 192, 192, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom',
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.label || '';
                            let value = context.raw || 0;
                            let gbValue = (value / 1073741824).toFixed(2);
                            return label + ': ' + gbValue + ' GB';
                        }
                    }
                }
            }
        }
    });
</script>
</body>
</html>
