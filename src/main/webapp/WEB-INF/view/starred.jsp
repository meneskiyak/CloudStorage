<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="tr">
<head>
    <title><spring:message code="starred.title"/> - <spring:message code="app.name"/></title>
    <%@ include file="_layout_head.jsp" %>
</head>
<body>

<c:set var="activeNav" value="starred" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<main class="main">
    <div class="breadcrumb-area mb-4">
        <h4 class="mb-0"><spring:message code="starred.title"/></h4>
    </div>

    <%-- Folders section --%>
    <c:if test="${not empty folders}">
        <div class="section-title"><spring:message code="dashboard.section.folders"/></div>
        <div class="folder-grid">
            <c:forEach var="folder" items="${folders}">
                <div class="folder-card" onclick="location.href='${pageContext.request.contextPath}/dashboard?folderId=${folder.id}'">
                    <i class="bi bi-folder-fill folder-icon"></i>
                    <span class="folder-name">${folder.name}</span>
                    <i class="bi bi-star-fill text-warning ms-auto" style="font-size: 0.9rem;"></i>
                    <button class="card-menu-btn" data-bs-toggle="dropdown" aria-expanded="false" onclick="event.stopPropagation()">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-1">
                        <li>
                            <form action="${pageContext.request.contextPath}/folder/star" method="post" class="m-0">
                                <input type="hidden" name="folderId" value="${folder.id}">
                                <input type="hidden" name="redirect" value="starred">
                                <button type="submit" class="dropdown-item small py-2">
                                    <i class="bi bi-star me-2"></i><spring:message code="common.unstar"/>
                                </button>
                            </form>
                        </li>
                    </ul>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <%-- Files section --%>
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
                    <span class="folder-name" title="${file.originalName}">${file.originalName}</span>
                    <i class="bi bi-star-fill text-warning ms-auto" style="font-size: 0.9rem;"></i>
                    <button class="card-menu-btn" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-1">
                        <li>
                            <a class="dropdown-item small py-2" href="${pageContext.request.contextPath}/file/download?fileId=${file.id}">
                                <i class="bi bi-download me-2"></i><spring:message code="common.download"/>
                            </a>
                        </li>
                        <li>
                            <form action="${pageContext.request.contextPath}/file/star" method="post" class="m-0">
                                <input type="hidden" name="fileId" value="${file.id}">
                                <input type="hidden" name="redirect" value="starred">
                                <button type="submit" class="dropdown-item small py-2">
                                    <i class="bi bi-star me-2"></i><spring:message code="common.unstar"/>
                                </button>
                            </form>
                        </li>
                    </ul>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${empty folders and empty files}">
        <div class="empty-state">
            <i class="bi bi-star"></i>
            <p><spring:message code="starred.empty"/></p>
        </div>
    </c:if>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
