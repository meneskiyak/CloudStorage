<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="tr">
<head>
    <title><spring:message code="search.title"/> - <spring:message code="app.name"/></title>
    <%@ include file="_layout_head.jsp" %>
</head>
<body>

<c:set var="activeNav" value="" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<main class="main">

    <div class="breadcrumb-area mb-4">
        <h4 class="mb-0">
            <spring:message code="search.title"/>:
            <span style="color:var(--accent)">${fn:escapeXml(query)}</span>
        </h4>
    </div>

    <c:choose>
        <c:when test="${empty query or fn:trim(query) == ''}">
            <div class="empty-state">
                <i class="bi bi-search"></i>
                <p><spring:message code="search.hint"/></p>
            </div>
        </c:when>
        <c:when test="${empty folders and empty files}">
            <div class="empty-state">
                <i class="bi bi-search"></i>
                <p><spring:message code="search.noResults"/></p>
            </div>
        </c:when>
        <c:otherwise>

            <%-- Klasörler --%>
            <c:if test="${not empty folders}">
                <div class="section-title"><spring:message code="dashboard.section.folders"/></div>
                <div class="folder-grid">
                    <c:forEach var="folder" items="${folders}">
                        <a class="folder-card"
                           href="${pageContext.request.contextPath}/dashboard?folderId=${folder.id}">
                            <i class="bi bi-folder-fill folder-icon"></i>
                            <span class="folder-name">${fn:escapeXml(folder.name)}</span>
                            <c:if test="${folder.starred}">
                                <i class="bi bi-star-fill text-warning ms-auto" style="font-size:0.9rem"></i>
                            </c:if>
                        </a>
                    </c:forEach>
                </div>
            </c:if>

            <%-- Dosyalar --%>
            <c:if test="${not empty files}">
                <div class="section-title"><spring:message code="dashboard.section.files"/></div>
                <div class="folder-grid">
                    <c:forEach var="file" items="${files}">
                        <div class="folder-card">
                            <c:choose>
                                <c:when test="${fn:startsWith(file.mimeType, 'image/')}"><i class="bi bi-file-image folder-icon"></i></c:when>
                                <c:when test="${file.mimeType == 'application/pdf'}"><i class="bi bi-file-pdf folder-icon"></i></c:when>
                                <c:when test="${fn:startsWith(file.mimeType, 'video/')}"><i class="bi bi-file-play folder-icon"></i></c:when>
                                <c:when test="${fn:startsWith(file.mimeType, 'audio/')}"><i class="bi bi-file-music folder-icon"></i></c:when>
                                <c:when test="${fn:contains(file.mimeType, 'zip') or fn:contains(file.mimeType, 'compressed')}"><i class="bi bi-file-zip folder-icon"></i></c:when>
                                <c:when test="${fn:contains(file.mimeType, 'word')}"><i class="bi bi-file-word folder-icon"></i></c:when>
                                <c:when test="${fn:contains(file.mimeType, 'sheet') or fn:contains(file.mimeType, 'excel')}"><i class="bi bi-file-excel folder-icon"></i></c:when>
                                <c:otherwise><i class="bi bi-file-earmark folder-icon"></i></c:otherwise>
                            </c:choose>
                            <span class="folder-name" title="${fn:escapeXml(file.originalName)}">${fn:escapeXml(file.originalName)}</span>
                            <c:if test="${file.starred}">
                                <i class="bi bi-star-fill text-warning ms-auto" style="font-size:0.9rem"></i>
                            </c:if>
                            <button class="card-menu-btn" data-bs-toggle="dropdown" aria-expanded="false"
                                    onclick="event.stopPropagation()">
                                <i class="bi bi-three-dots-vertical"></i>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-1">
                                <li>
                                    <a class="dropdown-item small py-2"
                                       href="${pageContext.request.contextPath}/file/download?fileId=${file.id}">
                                        <i class="bi bi-download me-2"></i><spring:message code="common.download"/>
                                    </a>
                                </li>
                                <c:if test="${file.folder != null}">
                                    <li>
                                        <a class="dropdown-item small py-2"
                                           href="${pageContext.request.contextPath}/dashboard?folderId=${file.folder.id}">
                                            <i class="bi bi-folder2-open me-2"></i><spring:message code="search.goToFolder"/>
                                        </a>
                                    </li>
                                </c:if>
                                <c:if test="${file.folder == null}">
                                    <li>
                                        <a class="dropdown-item small py-2"
                                           href="${pageContext.request.contextPath}/dashboard">
                                            <i class="bi bi-house-door me-2"></i><spring:message code="search.goToFolder"/>
                                        </a>
                                    </li>
                                </c:if>
                            </ul>
                        </div>
                    </c:forEach>
                </div>
            </c:if>

        </c:otherwise>
    </c:choose>

</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
