<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <title><spring:message code="admin.dashboard.title"/> | CloudStorage</title>
    <%@ include file="_layout_head.jsp" %>
    <style>
        .admin-card {
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 32px;
            transition: all 0.3s ease;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
        }
        .admin-card:hover {
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            transform: translateY(-4px);
            border-color: var(--accent);
        }
        .admin-icon-wrap {
            width: 80px;
            height: 80px;
            background: #e8f0fe;
            color: var(--accent);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2.5rem;
            margin-bottom: 20px;
        }
        .admin-btn {
            background-color: var(--accent);
            color: white;
            border: none;
            padding: 10px 24px;
            border-radius: 24px;
            font-weight: 500;
            margin-top: auto;
            width: 100%;
            transition: background 0.2s;
        }
        .admin-btn:hover {
            background-color: #1765cc;
            color: white;
        }
        .page-header-title {
            color: #3c4043;
            font-weight: 500;
            margin-bottom: 24px;
        }
    </style>
</head>
<body>

<c:set var="activeNav" value="admin" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<main class="main">
    <div class="breadcrumb-area mb-4">
        <h4 class="page-header-title"><spring:message code="admin.dashboard.title"/></h4>
    </div>

    <div class="row g-4">
        <%-- Kullanıcı Yönetimi Kartı --%>
        <div class="col-md-6 col-lg-5">
            <div class="admin-card shadow-sm">
                <div class="admin-icon-wrap">
                    <i class="bi bi-people-fill"></i>
                </div>
                <h5 class="fw-bold mb-3"><spring:message code="admin.dashboard.users.title"/></h5>
                <p class="text-secondary mb-4"><spring:message code="admin.dashboard.users.desc"/></p>
                <a href="${pageContext.request.contextPath}/admin/users" class="btn admin-btn">
                    <i class="bi bi-person-gear me-2"></i><spring:message code="admin.dashboard.users.btn"/>
                </a>
            </div>
        </div>

        <%-- Sistem İstatistikleri Kartı --%>
        <div class="col-md-6 col-lg-5">
            <div class="admin-card shadow-sm">
                <div class="admin-icon-wrap" style="background: #fef7e0; color: #f29900;">
                    <i class="bi bi-pie-chart-fill"></i>
                </div>
                <h5 class="fw-bold mb-3"><spring:message code="admin.dashboard.stats.title"/></h5>
                <p class="text-secondary mb-4"><spring:message code="admin.dashboard.stats.desc"/></p>
                <a href="${pageContext.request.contextPath}/admin/stats" class="btn admin-btn" style="background-color: #f29900;">
                    <i class="bi bi-graph-up-arrow me-2"></i><spring:message code="admin.dashboard.stats.btn"/>
                </a>
            </div>
        </div>
    </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
