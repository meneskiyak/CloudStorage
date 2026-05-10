<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sunucu Hatası - Cloud Storage</title>
    <style>
        body {
            font-family: 'Roboto', Arial, sans-serif;
            background-color: #f8f9fa;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .error-card {
            background: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24);
            text-align: center;
            max-width: 400px;
            width: 90%;
        }
        .error-icon {
            font-size: 64px;
            color: #fbbc04;
            margin-bottom: 20px;
        }
        h1 {
            color: #3c4043;
            font-size: 24px;
            margin-bottom: 16px;
        }
        p {
            color: #5f6368;
            font-size: 16px;
            line-height: 1.5;
            margin-bottom: 30px;
        }
        .btn-primary {
            display: inline-block;
            background-color: #1a73e8;
            color: white;
            padding: 10px 24px;
            border-radius: 4px;
            text-decoration: none;
            font-weight: 500;
            transition: background-color 0.2s;
        }
        .btn-primary:hover {
            background-color: #1765cc;
        }
    </style>
</head>
<body>
    <div class="error-card">
        <div class="error-icon">⚠️</div>
        <h1>500 - Sunucu Hatası</h1>
        <p>Sunucumuzda beklenmedik bir problem oluştu, mühendislerimiz bilgilendirildi</p>
        <a href="<c:url value='/dashboard'/>" class="btn-primary">Ana Sayfaya Dön</a>
    </div>
</body>
</html>
