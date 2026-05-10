<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <title><spring:message code="common.settings"/> | <spring:message code="app.name"/></title>
    <%@ include file="_layout_head.jsp" %>
    <style>
        .settings-card {
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 24px;
            max-width: 600px;
            margin-top: 20px;
        }
        .settings-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 16px 0;
        }
        .settings-row:not(:last-child) {
            border-bottom: 1px solid #f1f3f4;
        }
        .settings-label {
            font-weight: 500;
            color: #3c4043;
        }
        
        /* ── Modern Toggle Switch ── */
        .lang-switch-wrapper {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .lang-label {
            font-size: 0.9rem;
            font-weight: 500;
            color: #5f6368;
            cursor: pointer;
            transition: color 0.2s;
        }
        .lang-label.active {
            color: var(--accent);
        }
        
        .switch {
            position: relative;
            display: inline-block;
            width: 48px;
            height: 24px;
        }
        .switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }
        .slider {
            position: absolute;
            cursor: pointer;
            top: 0; left: 0; right: 0; bottom: 0;
            background-color: #bdc1c6;
            transition: .3s;
            border-radius: 24px;
        }
        .slider:before {
            position: absolute;
            content: "";
            height: 18px;
            width: 18px;
            left: 3px;
            bottom: 3px;
            background-color: white;
            transition: .3s;
            border-radius: 50%;
            box-shadow: 0 1px 3px rgba(0,0,0,0.2);
        }
        input:checked + .slider {
            background-color: var(--accent);
        }
        input:checked + .slider:before {
            transform: translateX(24px);
        }
    </style>
</head>
<body>

<c:set var="activeNav" value="settings" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<main class="main">
    <div class="breadcrumb-area">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item">
                    <a href="${pageContext.request.contextPath}/dashboard">
                        <spring:message code="dashboard.breadcrumb.home"/>
                    </a>
                </li>
                <li class="breadcrumb-item active"><spring:message code="common.settings"/></li>
            </ol>
        </nav>
    </div>

    <h4 class="mb-4"><spring:message code="common.settings"/></h4>

    <div class="settings-card shadow-sm">
        <div class="settings-row">
            <div>
                <div class="settings-label"><spring:message code="settings.language.label"/></div>
                <div class="text-secondary small"><spring:message code="settings.language.desc"/></div>
            </div>
            
            <div class="lang-switch-wrapper">
                <span class="lang-label ${pageContext.response.locale.language == 'en' ? 'active' : ''}" onclick="changeLang('en')">English</span>
                <label class="switch">
                    <input type="checkbox" id="langToggle" ${pageContext.response.locale.language == 'tr' ? 'checked' : ''}>
                    <span class="slider"></span>
                </label>
                <span class="lang-label ${pageContext.response.locale.language == 'tr' ? 'active' : ''}" onclick="changeLang('tr')">Türkçe</span>
            </div>
        </div>
        
        <div class="settings-row">
            <div>
                <div class="settings-label"><spring:message code="settings.account.label"/></div>
                <div class="text-secondary small">${user.fullName} (${user.email})</div>
            </div>
            <i class="bi bi-person-circle text-secondary" style="font-size: 1.5rem;"></i>
        </div>
    </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const langToggle = document.getElementById('langToggle');
    
    function changeLang(lang) {
        const url = new URL(window.location.href);
        url.searchParams.set('lang', lang);
        window.location.href = url.toString();
    }

    langToggle.addEventListener('change', function() {
        const newLang = this.checked ? 'tr' : 'en';
        changeLang(newLang);
    });
</script>

</body>
</html>
