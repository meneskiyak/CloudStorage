<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="app.name"/> | <spring:message code="auth.login.title"/></title>
    
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
        }

        .auth-card {
            background: #ffffff;
            border-radius: var(--border-radius-card);
            box-shadow: var(--card-shadow);
            width: 100%;
            max-width: 420px;
            padding: 40px;
        }

        .brand-header {
            text-align: center;
            margin-bottom: 30px;
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
        }

        .form-control {
            border-radius: var(--border-radius-input);
            padding: 12px 16px;
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
            margin-top: 10px;
        }

        .btn-google:hover {
            background-color: #1765cc;
            color: white;
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

        .form-check-label {
            font-size: 0.85rem;
            color: #5f6368;
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
        <spring:message code="auth.login.title"/>
    </h5>

    <%-- Bildirim Alanları --%>
    <c:if test="${not empty param.registered}">
        <div class="alert alert-success alert-modern">
            <i class="fa-solid fa-circle-check me-2"></i>
            <spring:message code="auth.login.registered"/>
        </div>
    </c:if>
    <c:if test="${not empty param.logout}">
        <div class="alert alert-info alert-modern">
            <i class="fa-solid fa-circle-info me-2"></i>
            <spring:message code="auth.login.logout"/>
        </div>
    </c:if>
    <c:if test="${not empty error or not empty param.error}">
        <div class="alert alert-danger alert-modern">
            <i class="fa-solid fa-circle-exclamation me-2"></i>
            <c:choose>
                <c:when test="${param.error == 'invalid'}"><spring:message code="error.invalid"/></c:when>
                <c:otherwise>${error}</c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/login" method="POST">
        <div class="mb-3">
            <label class="form-label"><spring:message code="auth.login.email"/></label>
            <input type="email" name="email" class="form-control" placeholder="example@email.com" required>
        </div>
        <div class="mb-3">
            <label class="form-label"><spring:message code="auth.login.password"/></label>
            <input type="password" name="password" class="form-control" placeholder="••••••••" required>
        </div>
        <div class="mb-3 form-check">
            <input type="checkbox" name="rememberMe" class="form-check-input" id="rememberMe">
            <label class="form-check-label" for="rememberMe">
                <spring:message code="auth.login.rememberMe"/>
            </label>
        </div>
        <button type="submit" class="btn btn-google">
            <spring:message code="auth.login.submit"/>
        </button>
    </form>

    <div class="auth-footer">
        <spring:message code="auth.login.noAccount"/> 
        <a href="${pageContext.request.contextPath}/register">
            <spring:message code="auth.login.registerNow"/>
        </a>
    </div>
</div>

</body>
</html>
