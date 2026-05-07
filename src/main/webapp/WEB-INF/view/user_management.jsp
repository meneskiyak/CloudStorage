<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Admin | Kullanıcı Yönetimi</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-dark bg-primary mb-4">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/admin/dashboard">Admin Paneli</a>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline-light btn-sm">Geri Dön</a>
    </div>
</nav>

<div class="container">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Kullanıcı Listesi</h2>
        <c:if test="${param.success == 'role'}">
            <div class="alert alert-success py-1 px-3 mb-0">Rol başarıyla güncellendi!</div>
        </c:if>
        <c:if test="${param.success == 'quota'}">
            <div class="alert alert-success py-1 px-3 mb-0">Kota başarıyla güncellendi!</div>
        </c:if>
    </div>

    <div class="card shadow-sm">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Ad Soyad</th>
                    <th>Email</th>
                    <th>Mevcut Rol</th>
                    <th>Mevcut Kota (GB)</th>
                    <th class="text-end">İşlemler</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="u" items="${users}">
                    <tr>
                        <td>${u.fullName}</td>
                        <td>${u.email}</td>
                        <td>
                            <span class="badge ${u.role == 'ADMIN' ? 'bg-danger' : 'bg-info'}">${u.role}</span>
                        </td>
                        <td>
                            <fmt:formatNumber value="${u.uploadLimitBytes / 1073741824}" maxFractionDigits="1"/> GB
                        </td>
                        <td class="text-end">
                            <!-- Rol Düzenleme Formu (Kendi rolünü değiştiremez) -->
                            <form action="${pageContext.request.contextPath}/admin/users/update-role" method="POST" class="d-inline-block me-2">
                                <input type="hidden" name="userId" value="${u.id}">
                                <select name="role" class="form-select form-select-sm d-inline-block w-auto" 
                                        onchange="this.form.submit()" 
                                        ${u.id == currentUser.id ? 'disabled' : ''}>
                                    <option value="USER" ${u.role == 'USER' ? 'selected' : ''}>USER Yap</option>
                                    <option value="ADMIN" ${u.role == 'ADMIN' ? 'selected' : ''}>ADMIN Yap</option>
                                </select>
                                <c:if test="${u.id == currentUser.id}">
                                    <small class="d-block text-muted" style="font-size: 0.7em;">Kendi rolünüzü değiştiremezsiniz.</small>
                                </c:if>
                            </form>

                            <!-- Kota Düzenleme Butonu -->
                            <button class="btn btn-sm btn-outline-primary" data-bs-toggle="modal" data-bs-target="#quotaModal${u.id}">
                                Kota Belirle
                            </button>

                            <!-- Kota Modal -->
                            <div class="modal fade" id="quotaModal${u.id}" tabindex="-1">
                                <div class="modal-dialog">
                                    <div class="modal-content text-start">
                                        <form action="${pageContext.request.contextPath}/admin/users/update-quota" method="POST">
                                            <div class="modal-header">
                                                <h5 class="modal-title">Kota Düzenle: ${u.fullName}</h5>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                            </div>
                                            <div class="modal-body">
                                                <input type="hidden" name="userId" value="${u.id}">
                                                <div class="mb-3">
                                                    <label class="form-label">Yeni Kota (GB Cinsinden)</label>
                                                    <input type="number" name="quotaGb" class="form-control" value="<fmt:formatNumber value='${u.uploadLimitBytes / 1073741824}' maxFractionDigits='0'/>" min="1" required>
                                                </div>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">İptal</button>
                                                <button type="submit" class="btn btn-primary">Kaydet</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
