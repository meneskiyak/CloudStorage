<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Bulut Depolama | Kayıt Ol</title>
    <!-- Tasarım için hızlıca Bootstrap ekledik -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-4">
            <h3 class="text-center mb-4">Yeni Hesap Oluştur</h3>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <div class="card shadow-sm">
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/register" method="POST">
                        <div class="mb-3">
                            <label>Ad Soyad</label>
                            <input type="text" name="fullName" class="form-control" required>
                        </div>
                        <div class="mb-3">
                            <label>Kullanıcı Adı</label>
                            <input type="text" name="username" class="form-control" required>
                        </div>
                        <div class="mb-3">
                            <label>Email</label>
                            <input type="email" name="email" class="form-control" required>
                        </div>
                        <div class="mb-3">
                            <label>Şifre</label>
                            <!-- Entity'de "passwordHash" olarak tanımlamışsın, name o yüzden passwordHash -->
                            <input type="password" name="passwordHash" class="form-control" required>
                        </div>
                        <button type="submit" class="btn btn-primary w-100">Kayıt Ol</button>
                    </form>
                </div>
            </div>
            <p class="text-center mt-3">Zaten hesabın var mı? <a href="${pageContext.request.contextPath}/login">Giriş Yap</a></p>
        </div>
    </div>
</div>
</body>
</html>