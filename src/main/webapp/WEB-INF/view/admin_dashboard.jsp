<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Bulut Depolama | Admin Paneli</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-dark text-white">
<nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
    <div class="container">
        <a class="navbar-brand" href="#">CloudStorage Admin</a>
        <div class="d-flex">
            <span class="navbar-text me-3">Hoş geldin, ${adminUser.fullName}</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">Çıkış Yap</a>
        </div>
    </div>
</nav>

<div class="container mt-5">
    <div class="row">
        <div class="col-md-12">
            <h2>Yönetici Kontrol Paneli</h2>
            <hr class="bg-light">
            <div class="row mt-4">
                <div class="col-md-6">
                    <div class="card bg-secondary text-white shadow-sm mb-4">
                        <div class="card-body text-center">
                            <h5 class="card-title">Kullanıcı Yönetimi</h5>
                            <p class="card-text">Tüm kullanıcıları görüntüle, kota ayarlarını düzenle.</p>
                            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-light w-100">Yönet</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card bg-secondary text-white shadow-sm mb-4">
                        <div class="card-body text-center">
                            <h5 class="card-title">Sistem İstatistikleri</h5>
                            <p class="card-text">Toplam dosya sayısı ve kullanılan alan bilgileri.</p>
                            <a href="${pageContext.request.contextPath}/admin/stats" class="btn btn-info text-white w-100">İstatistikleri Gör</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
