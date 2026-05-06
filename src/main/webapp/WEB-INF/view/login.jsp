<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Bulut Depolama | Giriş Yap</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-4">
            <h3 class="text-center mb-4">Sisteme Giriş</h3>

            <%-- Parametrelerden gelen mesajları yakalama --%>
            <% if(request.getParameter("registered") != null) { %>
            <div class="alert alert-success">Kayıt başarılı! Lütfen giriş yapın.</div>
            <% } %>
            <% if(request.getParameter("logout") != null) { %>
            <div class="alert alert-info">Başarıyla çıkış yaptınız.</div>
            <% } %>
            <% if(request.getAttribute("error") != null) { %>
            <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <div class="card shadow-sm">
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/login" method="POST">
                        <div class="mb-3">
                            <label>Email Adresi</label>
                            <input type="email" name="email" class="form-control" required>
                        </div>
                        <div class="mb-3">
                            <label>Şifre</label>
                            <input type="password" name="password" class="form-control" required>
                        </div>
                        <button type="submit" class="btn btn-success w-100">Giriş Yap</button>
                    </form>
                </div>
            </div>
            <p class="text-center mt-3">Hesabın yok mu? <a href="${pageContext.request.contextPath}/register">Kayıt Ol</a></p>
        </div>
    </div>
</div>
</body>
</html>