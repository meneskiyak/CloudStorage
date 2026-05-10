<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <title><spring:message code="admin.users.title"/> | CloudStorage</title>
    <%@ include file="_layout_head.jsp" %>
    <style>
        .users-card {
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 12px;
            overflow: hidden;
        }
        .table thead th {
            background-color: #f8f9fa;
            color: #5f6368;
            font-size: 0.8rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            padding: 16px 24px;
            border-bottom: 1px solid #e8eaed;
        }
        .table tbody td {
            padding: 12px 24px;
            color: #3c4043;
            border-bottom: 1px solid #f1f3f4;
            vertical-align: middle;
        }
        .table tbody tr:hover {
            background-color: #f8f9fa;
        }
        
        /* Material Chips (Badges) */
        .role-chip {
            display: inline-flex;
            align-items: center;
            padding: 4px 12px;
            border-radius: 16px;
            font-size: 0.75rem;
            font-weight: 500;
        }
        .role-chip-user {
            background-color: #e8f0fe;
            color: #1967d2;
        }
        .role-chip-admin {
            background-color: #fce8e6;
            color: #c5221f;
        }
        
        /* Modern Select & Buttons */
        .modern-select {
            border-radius: 8px;
            font-size: 0.85rem;
            padding: 4px 8px;
            border: 1px solid #dadce0;
            color: #3c4043;
            outline: none;
            transition: border-color 0.2s;
        }
        .modern-select:focus {
            border-color: var(--accent);
        }
        .modern-select:disabled {
            background-color: #f1f3f4;
            color: #9aa0a6;
            cursor: not-allowed;
            border-color: #e8eaed;
        }
        .btn-google {
            border-radius: 20px;
            font-size: 0.85rem;
            font-weight: 500;
            padding: 6px 16px;
            transition: all 0.2s;
        }
        .btn-google-outline {
            border: 1px solid #dadce0;
            color: var(--accent);
            background: #fff;
        }
        .btn-google-outline:hover {
            background: #f1f3f4;
            border-color: #dadce0;
            color: #1765cc;
        }
        .self-warning {
            font-size: 0.7rem;
            color: #ea4335;
            margin-top: 4px;
            display: block;
        }
    </style>
</head>
<body>

<c:set var="activeNav" value="admin" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<main class="main">
    <div class="breadcrumb-area d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="mb-1"><spring:message code="admin.users.title"/></h4>
            <div class="text-secondary small"><spring:message code="admin.users.desc"/></div>
        </div>
        
        <div class="d-flex gap-2">
            <c:if test="${param.success == 'role'}">
                <div class="alert alert-success py-2 px-3 mb-0 small rounded-pill border-0 shadow-sm">
                    <i class="bi bi-check-circle-fill me-2"></i><spring:message code="admin.users.success.role"/>
                </div>
            </c:if>
            <c:if test="${param.success == 'quota'}">
                <div class="alert alert-success py-2 px-3 mb-0 small rounded-pill border-0 shadow-sm">
                    <i class="bi bi-check-circle-fill me-2"></i><spring:message code="admin.users.success.quota"/>
                </div>
            </c:if>
        </div>
    </div>

    <div class="users-card shadow-sm">
        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead>
                <tr>
                    <th><spring:message code="admin.users.table.name"/></th>
                    <th><spring:message code="admin.users.table.email"/></th>
                    <th><spring:message code="admin.users.table.role"/></th>
                    <th><spring:message code="admin.users.table.quota"/></th>
                    <th class="text-end"><spring:message code="admin.users.table.actions"/></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="u" items="${users}">
                    <tr>
                        <td class="fw-medium">${u.fullName}</td>
                        <td class="text-secondary">${u.email}</td>
                        <td>
                            <div class="role-chip ${u.role == 'ADMIN' ? 'role-chip-admin' : 'role-chip-user'}">
                                <i class="bi ${u.role == 'ADMIN' ? 'bi-shield-check' : 'bi-person'} me-1"></i>
                                ${u.role}
                            </div>
                        </td>
                        <td>
                            <c:set var="limit" value="${u.uploadLimitBytes > 0 ? u.uploadLimitBytes : 1}"/>
                            <c:set var="pct" value="${(u.usedBytes / limit) * 100}"/>
                            <c:set var="barColor" value="${pct < 50 ? 'bg-success' : (pct < 90 ? 'bg-warning' : 'bg-danger')}"/>
                            
                            <div class="d-flex align-items-center gap-2" style="min-width: 150px;">
                                <div class="progress flex-grow-1" style="height: 6px; background-color: #e8eaed;">
                                    <div class="progress-bar ${barColor}" role="progressbar" style="width: ${pct}%"></div>
                                </div>
                                <span class="fw-bold small text-nowrap">
                                    <fmt:formatNumber value="${u.uploadLimitBytes / 1073741824}" maxFractionDigits="1"/> GB
                                </span>
                            </div>
                            <div class="text-secondary" style="font-size: 0.7rem;">
                                <fmt:formatNumber value="${u.usedBytes / 1073741824}" maxFractionDigits="2"/> GB kullanılıyor
                            </div>
                        </td>
                        <td class="text-end">
                            <div class="d-flex align-items-center justify-content-end gap-3">
                                <%-- Rol Düzenleme --%>
                                <form action="${pageContext.request.contextPath}/admin/users/update-role" method="POST" class="m-0">
                                    <input type="hidden" name="userId" value="${u.id}">
                                    <select name="role" class="modern-select" 
                                            onchange="this.form.submit()" 
                                            ${u.id == user.id ? 'disabled' : ''}>
                                        <option value="USER" ${u.role == 'USER' ? 'selected' : ''}><spring:message code="admin.users.role.user"/></option>
                                        <option value="ADMIN" ${u.role == 'ADMIN' ? 'selected' : ''}><spring:message code="admin.users.role.admin"/></option>
                                    </select>
                                    <c:if test="${u.id == user.id}">
                                        <span class="self-warning"><spring:message code="admin.users.role.selfWarning"/></span>
                                    </c:if>
                                </form>

                                <%-- Kota Butonu --%>
                                <button class="btn btn-google btn-google-outline" data-bs-toggle="modal" data-bs-target="#quotaModal${u.id}">
                                    <i class="bi bi-hdd-network me-1"></i><spring:message code="admin.users.quota.set"/>
                                </button>
                            </div>

                            <%-- Kota Modal --%>
                            <div class="modal fade" id="quotaModal${u.id}" tabindex="-1">
                                <div class="modal-dialog modal-dialog-centered" style="max-width: 400px;">
                                    <div class="modal-content border-0 shadow-lg" style="border-radius: 16px;">
                                        <form action="${pageContext.request.contextPath}/admin/users/update-quota" method="POST">
                                            <div class="modal-header border-0 pt-4 px-4">
                                                <h5 class="modal-title fw-bold">
                                                    <spring:message code="admin.users.quota.modalTitle"/>
                                                </h5>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                            </div>
                                            <div class="modal-body px-4">
                                                <div class="text-secondary small mb-3">${u.fullName} (${u.email})</div>
                                                <input type="hidden" name="userId" value="${u.id}">
                                                <div class="mb-3">
                                                    <label class="form-label small fw-bold text-secondary">
                                                        <spring:message code="admin.users.quota.newLimit"/>
                                                    </label>
                                                    <input type="number" name="quotaGb" class="form-control form-control-lg border-2" 
                                                           value="<fmt:formatNumber value='${u.uploadLimitBytes / 1073741824}' maxFractionDigits='0'/>" 
                                                           min="1" required style="border-radius: 10px;">
                                                </div>
                                            </div>
                                            <div class="modal-footer border-0 pb-4 px-4">
                                                <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">
                                                    <spring:message code="common.cancel"/>
                                                </button>
                                                <button type="submit" class="btn btn-primary rounded-pill px-4">
                                                    <spring:message code="dashboard.modal.newFolder.create"/>
                                                </button>
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
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
