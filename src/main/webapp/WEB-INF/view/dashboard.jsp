<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Ana Sayfa</title></head>
<body>
<h2>Hoş Geldin, ${user.fullName}!</h2>
<p>Kullanılan Alan: ${user.usedBytes} / ${user.uploadLimitBytes} Byte</p>
<a href="${pageContext.request.contextPath}/logout">Çıkış Yap</a>
</body>
</html>