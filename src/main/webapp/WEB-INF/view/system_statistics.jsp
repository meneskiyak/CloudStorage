<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <title><spring:message code="admin.stats.title"/> | CloudStorage</title>
    <%@ include file="_layout_head.jsp" %>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { background-color: #f8fafc; }
        .kpi-card {
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 24px;
            display: flex;
            align-items: center;
            gap: 20px;
            transition: transform 0.2s;
        }
        .kpi-card:hover { transform: translateY(-3px); }
        .kpi-icon {
            width: 56px;
            height: 56px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
        }
        .kpi-value { font-size: 1.5rem; font-weight: 700; color: #1a1f36; }
        .kpi-label { font-size: 0.85rem; color: #697386; font-weight: 500; text-transform: uppercase; }
        
        .chart-card {
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 24px;
            height: 100%;
        }
        .chart-title {
            font-size: 1rem;
            font-weight: 600;
            color: #1a1f36;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .status-pill {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.75rem;
            font-weight: 600;
            background: #f1f3f4;
        }
        .dot { width: 8px; height: 8px; border-radius: 50%; }
        .dot-online { background-color: #34a853; box-shadow: 0 0 8px #34a853; }
        .dot-offline { background-color: #ea4335; box-shadow: 0 0 8px #ea4335; }

        .activity-table th {
            background: #f8fafc;
            font-size: 0.75rem;
            text-transform: uppercase;
            color: #697386;
            padding: 12px 20px;
        }
        .activity-table td { padding: 12px 20px; font-size: 0.85rem; vertical-align: middle; }
    </style>
</head>
<body>

<c:set var="activeNav" value="admin" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<main class="main">
    <div class="breadcrumb-area mb-4">
        <div>
            <h4 class="fw-bold mb-1"><spring:message code="admin.stats.title"/></h4>
            <div class="text-secondary small"><spring:message code="admin.stats.desc"/></div>
        </div>
    </div>

    <%-- KPI Kartları --%>
    <div class="row g-4 mb-4">
        <div class="col-md-4">
            <div class="kpi-card shadow-sm">
                <div class="kpi-icon" style="background: #e8f0fe; color: #1a73e8;">
                    <i class="bi bi-people-fill"></i>
                </div>
                <div>
                    <div class="kpi-label"><spring:message code="admin.stats.kpi.users"/></div>
                    <div class="kpi-value">${stats.userCount}</div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="kpi-card shadow-sm">
                <div class="kpi-icon" style="background: #e6fffa; color: #00897b;">
                    <i class="bi bi-cloud-check-fill"></i>
                </div>
                <div>
                    <div class="kpi-label"><spring:message code="admin.stats.kpi.capacity"/></div>
                    <div class="kpi-value"><fmt:formatNumber value="${stats.totalLimit / 1073741824}" maxFractionDigits="1"/> GB</div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="kpi-card shadow-sm">
                <div class="kpi-icon" style="background: #fff5f5; color: #e53e3e;">
                    <i class="bi bi-pie-chart-fill"></i>
                </div>
                <div>
                    <div class="kpi-label"><spring:message code="admin.stats.kpi.used"/></div>
                    <div class="kpi-value">
                        <c:choose>
                            <c:when test="${stats.totalUsed > 1073741824}">
                                <fmt:formatNumber value="${stats.totalUsed / 1073741824}" maxFractionDigits="2"/> GB
                            </c:when>
                            <c:otherwise>
                                <fmt:formatNumber value="${stats.totalUsed / 1048576}" maxFractionDigits="1"/> MB
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%-- Grafik Alanı --%>
    <div class="row g-4 mb-4">
        <div class="col-lg-5">
            <div class="chart-card shadow-sm">
                <div class="chart-title">
                    <spring:message code="admin.stats.chart.type"/>
                    <i class="bi bi-info-circle text-secondary small" title="Sistemdeki dosya tiplerinin oranı"></i>
                </div>
                <div style="height: 300px; position: relative;">
                    <canvas id="fileTypeChart"></canvas>
                </div>
            </div>
        </div>
        <div class="col-lg-7">
            <div class="chart-card shadow-sm">
                <div class="chart-title">
                    <spring:message code="admin.stats.chart.trend"/>
                </div>
                <div style="height: 300px; position: relative;">
                    <canvas id="usageTrendChart"></canvas>
                </div>
            </div>
        </div>
    </div>

    <%-- Son Aktiviteler --%>
    <div class="chart-card shadow-sm">
        <div class="chart-title"><spring:message code="admin.stats.activities.title"/></div>
        <div class="table-responsive">
            <table class="table activity-table mb-0">
                <thead>
                    <tr>
                        <th><spring:message code="admin.stats.activities.time"/></th>
                        <th><spring:message code="admin.stats.activities.user"/></th>
                        <th><spring:message code="admin.stats.activities.action"/></th>
                        <th><spring:message code="admin.stats.activities.status"/></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="activity" items="${stats.recentActivities}" varStatus="loop">
                        <tr>
                            <td class="text-secondary small"><fmt:formatDate value="${activity.updatedAt}" pattern="dd.MM.yyyy HH:mm"/></td>
                            <td class="fw-medium">${activity.owner.fullName}</td>
                            <td>
                                <span class="badge bg-light text-dark border">
                                    ${activity.originalName}
                                </span>
                            </td>
                            <td>
                                <span class="text-secondary small fw-bold">
                                    <i class="bi bi-clock-history me-1"></i>
                                    <c:set var="timeParts" value="${fn:split(timeAgos[loop.index], ',')}"/>
                                    <c:choose>
                                        <c:when test="${fn:length(timeParts) > 1}">
                                            <spring:message code="${timeParts[0]}" arguments="${timeParts[1]}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <spring:message code="${timeParts[0]}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</main>

<script>
    // ── Dosya Türü Dağılımı (Doughnut) ──
    const typeLabels = [
        <c:forEach var="entry" items="${stats.fileTypeCounts}" varStatus="loop">
            '<spring:message code="${entry.key}"/>'${!loop.last ? ',' : ''}
        </c:forEach>
    ];
    const typeData = [
        <c:forEach var="entry" items="${stats.fileTypeCounts}" varStatus="loop">
            ${entry.value}${!loop.last ? ',' : ''}
        </c:forEach>
    ];

    const typeCtx = document.getElementById('fileTypeChart').getContext('2d');
    new Chart(typeCtx, {
        type: 'doughnut',
        data: {
            labels: typeLabels,
            datasets: [{
                data: typeData,
                backgroundColor: ['#4285f4', '#ea4335', '#fbbc05', '#34a853', '#673ab7'],
                hoverOffset: 10,
                borderWidth: 0
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom', labels: { usePointStyle: true, padding: 20 } }
            },
            cutout: '70%'
        }
    });

    // ── Depolama Trendi (Area Chart) ──
    const trendLabels = [
        <c:forEach var="entry" items="${stats.usageTrend}" varStatus="loop">
            '${entry.key}'${!loop.last ? ',' : ''}
        </c:forEach>
    ];
    const trendData = [
        <c:forEach var="entry" items="${stats.usageTrend}" varStatus="loop">
            ${entry.value}${!loop.last ? ',' : ''}
        </c:forEach>
    ];

    const trendCtx = document.getElementById('usageTrendChart').getContext('2d');
    const gradient = trendCtx.createLinearGradient(0, 0, 0, 300);
    gradient.addColorStop(0, 'rgba(26, 115, 232, 0.2)');
    gradient.addColorStop(1, 'rgba(26, 115, 232, 0)');

    new Chart(trendCtx, {
        type: 'line',
        data: {
            labels: trendLabels,
            datasets: [{
                label: '<spring:message code="admin.stats.kpi.used"/>',
                data: trendData,
                borderColor: '#1a73e8',
                backgroundColor: gradient,
                fill: true,
                tension: 0.4,
                pointRadius: 4,
                pointBackgroundColor: '#fff',
                pointBorderWidth: 2
            }]
        },
        options: {
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, grid: { borderDash: [5, 5], drawBorder: false } },
                x: { grid: { display: false } }
            },
            plugins: { legend: { display: false } }
        }
    });
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
