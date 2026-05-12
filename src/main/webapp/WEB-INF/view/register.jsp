<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="app.name"/> | <spring:message code="auth.register.title"/></title>
    
    <!-- Favicon -->
    <link rel="icon" type="image/svg+xml" href="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>☁️</text></svg>">
    
    <!-- Google Fonts: Poppins -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600&display=swap" rel="stylesheet">
    <!-- FontAwesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <style>
        :root {
            --google-blue: #1a73e8;
            --bg-color: #f1f3f4;
            --card-shadow: 0 4px 12px rgba(0,0,0,0.05);
            --border-radius-card: 12px;
            --border-radius-input: 8px;
        }

        body {
            font-family: 'Poppins', sans-serif;
            background-color: var(--bg-color);
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            margin: 0;
            padding: 20px 0;
        }

        .auth-card {
            background: #ffffff;
            border-radius: var(--border-radius-card);
            box-shadow: var(--card-shadow);
            width: 100%;
            max-width: 450px;
            padding: 40px;
        }

        .brand-header {
            text-align: center;
            margin-bottom: 25px;
        }

        .brand-logo {
            font-size: 2.5rem;
            color: var(--google-blue);
            margin-bottom: 10px;
        }

        .brand-name {
            font-weight: 600;
            color: var(--google-blue);
            font-size: 1.5rem;
            letter-spacing: -0.5px;
        }

        .form-label {
            font-size: 0.9rem;
            font-weight: 500;
            color: #5f6368;
            margin-bottom: 6px;
        }

        .form-control {
            border-radius: var(--border-radius-input);
            padding: 10px 14px;
            border: 1px solid #dadce0;
            font-size: 0.95rem;
            transition: all 0.2s ease;
        }

        .form-control:focus {
            border-color: var(--google-blue);
            box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.2);
            outline: none;
        }

        .btn-google {
            background-color: var(--google-blue);
            color: white;
            border: none;
            border-radius: var(--border-radius-input);
            padding: 12px;
            font-weight: 500;
            transition: background-color 0.2s;
            width: 100%;
            margin-top: 15px;
        }

        .btn-google:hover:not(:disabled) {
            background-color: #1765cc;
            color: white;
        }

        .btn-google:disabled {
            background-color: #dadce0;
            cursor: not-allowed;
        }

        .auth-footer {
            text-align: center;
            margin-top: 25px;
            font-size: 0.9rem;
            color: #5f6368;
        }

        .auth-footer a {
            color: var(--google-blue);
            text-decoration: none;
            font-weight: 500;
        }

        .auth-footer a:hover {
            text-decoration: underline;
        }

        .alert-modern {
            border: none;
            border-radius: 8px;
            font-size: 0.85rem;
            padding: 12px;
            margin-bottom: 20px;
        }

        .validation-msg {
            font-size: 0.75rem;
            margin-top: 4px;
            display: none;
            color: #d93025;
        }

        .validation-msg.visible {
            display: block;
        }

        /* Bootstrap'in kendi ikonunu bastır, sadece border rengini kullan */
        .input-state-valid {
            border-color: #1e8e3e !important;
            background-image: none !important;
        }
        .input-state-invalid {
            border-color: #d93025 !important;
            background-image: none !important;
        }

        .password-wrapper {
            position: relative;
        }

        .password-wrapper .form-control {
            padding-right: 42px;
        }

        .toggle-password {
            position: absolute;
            right: 12px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            color: #5f6368;
            cursor: pointer;
            padding: 0;
            line-height: 1;
        }

        .toggle-password:hover {
            color: var(--google-blue);
        }
    </style>
</head>
<body>

<div class="auth-card">
    <div class="brand-header">
        <div class="brand-logo">
            <i class="fa-solid fa-cloud"></i>
        </div>
        <div class="brand-name">CloudStorage</div>
    </div>

    <h5 class="text-center mb-4" style="color: #202124; font-weight: 500;">
        <spring:message code="auth.register.title"/>
    </h5>

    <%-- Bildirim Alanları --%>
    <c:if test="${not empty error or not empty param.error}">
        <div class="alert alert-danger alert-modern">
            <i class="fa-solid fa-circle-exclamation me-2"></i>
            <c:choose>
                <c:when test="${param.error == 'exists'}"><spring:message code="error.exists"/></c:when>
                <c:otherwise>${error}</c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <form id="registerForm" action="${pageContext.request.contextPath}/register" method="POST" novalidate>
        <div class="mb-3">
            <label class="form-label"><spring:message code="auth.register.fullName"/></label>
            <input type="text" id="fullNameInput" name="fullName" class="form-control" value="${user.fullName}" required placeholder="John Doe">
            <div class="validation-msg" id="fullNameMsg">Ad Soyad boş bırakılamaz!</div>
        </div>
        
        <div class="mb-3">
            <label class="form-label"><spring:message code="auth.login.email"/></label>
            <input type="email" id="emailInput" name="email" class="form-control" value="${user.email}" required placeholder="example@email.com">
            <div class="validation-msg" id="emailMsg">Geçersiz e-posta formatı!</div>
        </div>

        <div class="mb-3">
            <label class="form-label"><spring:message code="auth.login.password"/></label>
            <div class="password-wrapper">
                <input type="password" id="passwordInput" name="passwordHash" class="form-control" required placeholder="••••••••">
                <button type="button" class="toggle-password" onclick="toggleVisibility('passwordInput', this)">
                    <i class="fa-solid fa-eye"></i>
                </button>
            </div>
            <div class="validation-msg" id="passwordMsg">
                Şifre en az 8 karakter; 1 büyük, 1 küçük harf, 1 rakam ve 1 özel karakter içermelidir.
            </div>
        </div>

        <div class="mb-3">
            <label class="form-label"><spring:message code="auth.register.confirmPassword"/></label>
            <div class="password-wrapper">
                <input type="password" id="confirmPasswordInput" class="form-control" required placeholder="••••••••">
                <button type="button" class="toggle-password" onclick="toggleVisibility('confirmPasswordInput', this)">
                    <i class="fa-solid fa-eye"></i>
                </button>
            </div>
            <div class="validation-msg" id="confirmPasswordMsg">
                <spring:message code="auth.register.passwordMismatch"/>
            </div>
        </div>

        <button type="submit" id="submitBtn" class="btn btn-google">
            <spring:message code="auth.register.submit"/>
        </button>
    </form>

    <div class="auth-footer">
        <spring:message code="auth.register.hasAccount"/> 
        <a href="${pageContext.request.contextPath}/login">
            <spring:message code="auth.register.loginNow"/>
        </a>
    </div>
</div>

<script>
    const fullNameInput = document.getElementById('fullNameInput');
    const emailInput = document.getElementById('emailInput');
    const passwordInput = document.getElementById('passwordInput');
    const confirmPasswordInput = document.getElementById('confirmPasswordInput');
    const confirmPasswordMsg = document.getElementById('confirmPasswordMsg');
    const submitBtn = document.getElementById('submitBtn');

    const emailRegex = /^[A-Za-z0-9çğıöşüÇĞİÖŞÜ+_.-]+@(.+)$/;
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?":{}|_<>\/-]).{8,}$/;

    function toggleVisibility(inputId, btn) {
        const input = document.getElementById(inputId);
        const icon = btn.querySelector('i');
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.replace('fa-eye', 'fa-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.replace('fa-eye-slash', 'fa-eye');
        }
    }

    function setFieldState(input, msgEl, isValid, hasValue) {
        input.classList.remove('input-state-valid', 'input-state-invalid');
        if (hasValue) {
            input.classList.add(isValid ? 'input-state-valid' : 'input-state-invalid');
        }
        if (msgEl) {
            msgEl.classList.toggle('visible', hasValue && !isValid);
        }
    }

    function validate() {
        const isFullNameValid = fullNameInput.value.trim().length > 0;
        const isEmailValid = emailRegex.test(emailInput.value);
        const isPasswordValid = passwordRegex.test(passwordInput.value);
        const passwordsMatch = passwordInput.value === confirmPasswordInput.value;
        const isConfirmNotEmpty = confirmPasswordInput.value.length > 0;

        setFieldState(fullNameInput, document.getElementById('fullNameMsg'), isFullNameValid, fullNameInput.value.length > 0);
        setFieldState(emailInput, document.getElementById('emailMsg'), isEmailValid, emailInput.value.length > 0);
        setFieldState(passwordInput, document.getElementById('passwordMsg'), isPasswordValid, passwordInput.value.length > 0);
        setFieldState(confirmPasswordInput, document.getElementById('confirmPasswordMsg'), passwordsMatch, isConfirmNotEmpty);

        submitBtn.disabled = !(isFullNameValid && isEmailValid && isPasswordValid && passwordsMatch && isConfirmNotEmpty);
    }

    fullNameInput.addEventListener('input', validate);
    emailInput.addEventListener('input', validate);
    passwordInput.addEventListener('input', validate);
    confirmPasswordInput.addEventListener('input', validate);

    validate();
</script>

</body>
</html>
