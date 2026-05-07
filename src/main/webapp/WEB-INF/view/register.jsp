<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Bulut Depolama | Kayıt Ol</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .validation-msg { font-size: 0.85em; margin-top: 5px; display: none; }
        .is-invalid + .validation-msg { display: block; color: #dc3545; }
        .is-valid + .validation-msg { display: none; }
    </style>
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-5">
            <h3 class="text-center mb-4">Yeni Hesap Oluştur</h3>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <div class="card shadow-sm">
                <div class="card-body">
                    <form id="registerForm" action="${pageContext.request.contextPath}/register" method="POST" novalidate>
                        <div class="mb-3">
                            <label class="form-label">Ad Soyad</label>
                            <input type="text" name="fullName" class="form-control" value="${user.fullName}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Email</label>
                            <input type="email" id="emailInput" name="email" class="form-control" value="${user.email}" required>
                            <div class="validation-msg" id="emailMsg">Geçersiz e-posta formatı!</div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Şifre</label>
                            <input type="password" id="passwordInput" name="passwordHash" class="form-control" required>
                            <div class="validation-msg" id="passwordMsg">
                                Şifre en az 8 karakter; 1 büyük, 1 küçük harf, 1 rakam ve 1 özel karakter içermelidir.
                            </div>
                        </div>
                        <button type="submit" id="submitBtn" class="btn btn-primary w-100 mt-2">Kayıt Ol</button>
                    </form>
                </div>
            </div>
            <p class="text-center mt-3">Zaten hesabın var mı? <a href="${pageContext.request.contextPath}/login">Giriş Yap</a></p>
        </div>
    </div>
</div>

<script>
    const emailInput = document.getElementById('emailInput');
    const passwordInput = document.getElementById('passwordInput');
    const submitBtn = document.getElementById('submitBtn');
    
    const emailRegex = /^[A-Za-z0-9çğıöşüÇĞİÖŞÜ+_.-]+@(.+)$/;
    // Alt çizgi (_) ve daha geniş sembol seti eklendi
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?":{}|_<>\/-]).{8,}$/;

    function validate() {
        const isEmailValid = emailRegex.test(emailInput.value);
        const isPasswordValid = passwordRegex.test(passwordInput.value);
        
        console.log("Email Valid:", isEmailValid, "Password Valid:", isPasswordValid);

        // Email UI
        if (emailInput.value.length > 0) {
            emailInput.classList.toggle('is-invalid', !isEmailValid);
            emailInput.classList.toggle('is-valid', isEmailValid);
        } else {
            emailInput.classList.remove('is-invalid', 'is-valid');
        }

        // Password UI
        if (passwordInput.value.length > 0) {
            passwordInput.classList.toggle('is-invalid', !isPasswordValid);
            passwordInput.classList.toggle('is-valid', isPasswordValid);
        } else {
            passwordInput.classList.remove('is-invalid', 'is-valid');
        }

        // Submit Button state
        submitBtn.disabled = !(isEmailValid && isPasswordValid);
    }

    emailInput.addEventListener('input', validate);
    passwordInput.addEventListener('input', validate);

    // İlk yüklemede butonu kontrol et
    validate();
</script>
</body>
</html>