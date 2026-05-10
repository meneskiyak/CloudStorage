<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <title><spring:message code="starred.title"/> - CloudStorage</title>
    <%@ include file="_layout_head.jsp" %>
</head>
<body>

<c:set var="activeNav" value="starred" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<main class="main">
    <div class="breadcrumb-area mb-4">
        <h4 class="mb-0"><spring:message code="starred.title"/></h4>
    </div>

    <%-- Alerts --%>
    <c:if test="${not empty error or param.error != null or not empty success}">
        <div class="alert ${not empty success ? 'alert-success' : 'alert-danger'} alert-dismissible fade show mb-4 shadow-sm border-0" role="alert" style="border-radius: 12px; background-color: ${not empty success ? '#e6f4ea' : '#fce8e6'}; color: ${not empty success ? '#137333' : '#c5221f'};">
            <i class="bi ${not empty success ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'} me-2"></i>
            <c:choose>
                <c:when test="${param.error == 'quota'}"><spring:message code="error.quota"/></c:when>
                <c:when test="${not empty error}">${error}</c:when>
                <c:when test="${not empty success}">${success}</c:when>
            </c:choose>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

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
                                    <i class="bi bi-star-fill text-warning me-2"></i><spring:message code="common.unstar"/>
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
                <c:set var="isImage" value="${fn:startsWith(file.mimeType, 'image/')}"/>
                <c:set var="isPdf"   value="${file.mimeType == 'application/pdf'}"/>
                <c:set var="canPrev" value="${isImage or isPdf}"/>

                <div class="folder-card ${canPrev ? 'has-thumb' : ''}"
                     onclick="${canPrev ? 'openPreviewModal('.concat(file.id).concat(',\'').concat(fn:escapeXml(file.originalName)).concat('\',\'').concat(file.mimeType).concat('\')') : 'void(0)'}">

                    <%-- Thumbnail alanı --%>
                    <c:if test="${canPrev}">
                        <div class="file-thumb-wrap">
                            <c:choose>
                                <c:when test="${isImage}">
                                    <img src="${pageContext.request.contextPath}/file/preview?fileId=${file.id}"
                                         alt="${fn:escapeXml(file.originalName)}"
                                         loading="lazy"
                                         onerror="this.style.display='none'">
                                </c:when>
                                <c:otherwise>
                                    <canvas class="pdf-thumb-canvas" data-pdf-url="${pageContext.request.contextPath}/file/preview?fileId=${file.id}"></canvas>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>

                    <%-- Meta satırı --%>
                    <div class="${canPrev ? 'card-meta-row' : 'd-flex align-items-center gap-2 flex-grow-1'}">
                        <c:choose>
                            <c:when test="${isImage}"><i class="bi bi-file-image folder-icon"></i></c:when>
                            <c:when test="${isPdf}"><i class="bi bi-file-pdf folder-icon" style="color:#ea4335;"></i></c:when>
                            <c:otherwise><i class="bi bi-file-earmark folder-icon"></i></c:otherwise>
                        </c:choose>
                        <span class="folder-name" title="${fn:escapeXml(file.originalName)}">${fn:escapeXml(file.originalName)}</span>
                        <i class="bi bi-star-fill text-warning" style="font-size:0.9rem;"></i>
                        <button class="card-menu-btn" data-bs-toggle="dropdown" aria-expanded="false"
                                onclick="event.stopPropagation();">
                            <i class="bi bi-three-dots-vertical"></i>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-1">
                            <c:if test="${canPrev}">
                                <li>
                                    <a class="dropdown-item small py-2" href="#"
                                       onclick="event.preventDefault(); event.stopPropagation();
                                                openPreviewModal(${file.id}, '${fn:escapeXml(file.originalName)}', '${file.mimeType}')">
                                        <i class="bi bi-eye me-2"></i><spring:message code="common.preview"/>
                                    </a>
                                </li>
                            </c:if>
                            <li>
                                <form action="${pageContext.request.contextPath}/file/star" method="post" class="m-0">
                                    <input type="hidden" name="fileId" value="${file.id}">
                                    <input type="hidden" name="redirect" value="starred">
                                    <button type="submit" class="dropdown-item small py-2" onclick="event.stopPropagation()">
                                        <i class="bi bi-star-fill text-warning me-2"></i><spring:message code="common.unstar"/>
                                    </button>
                                </form>
                            </li>
                            <li>
                                <a class="dropdown-item small py-2"
                                   href="${pageContext.request.contextPath}/file/download?fileId=${file.id}"
                                   onclick="event.stopPropagation();">
                                    <i class="bi bi-download me-2"></i><spring:message code="common.download"/>
                                </a>
                            </li>
                        </ul>
                    </div>
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

<%-- Modals Dashboard.jsp'den gelecek --%>
<div class="modal fade" id="previewModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content" style="border-radius:12px; overflow:hidden;">
            <div class="modal-header" style="background:#2d2d2d; border-bottom:1px solid #3c4043; padding:12px 20px;">
                <h6 class="modal-title text-white mb-0" id="previewModalTitle"></h6>
                <div class="d-flex gap-2 ms-auto">
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
            </div>
            <div class="modal-body" id="previewModalBody"></div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function openPreviewModal(fileId, fileName, mimeType) {
        const modalEl = document.getElementById('previewModal');
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        const body = document.getElementById('previewModalBody');
        const title = document.getElementById('previewModalTitle');
        const previewUrl = '${pageContext.request.contextPath}/file/preview?fileId=' + fileId;

        title.textContent = fileName;
        body.innerHTML = '<div class="d-flex align-items-center justify-content-center" style="min-height:300px;"><div class="spinner-border text-primary" role="status"></div></div>';
        modal.show();

        if (mimeType.startsWith('image/')) {
            const img = document.createElement('img');
            img.className = 'preview-img';
            img.src = previewUrl;
            img.onload = () => { body.innerHTML = ''; body.appendChild(img); };
        } else if (mimeType === 'application/pdf') {
            body.innerHTML = '<iframe class="preview-pdf-frame" src="' + previewUrl + '"></iframe>';
        }
    }
</script>
<script type="module">
    import { getDocument, GlobalWorkerOptions } from 'https://cdn.jsdelivr.net/npm/pdfjs-dist@4.4.168/build/pdf.min.mjs';
    GlobalWorkerOptions.workerSrc = 'https://cdn.jsdelivr.net/npm/pdfjs-dist@4.4.168/build/pdf.worker.min.mjs';

    document.querySelectorAll('canvas.pdf-thumb-canvas').forEach(canvas => {
        const url = canvas.dataset.pdfUrl;
        getDocument(url).promise.then(pdf => pdf.getPage(1)).then(page => {
            const wrap = canvas.parentElement;
            const viewport = page.getViewport({ scale: wrap.clientWidth / page.getViewport({ scale: 1 }).width });
            canvas.width = viewport.width; canvas.height = viewport.height;
            page.render({ canvasContext: canvas.getContext('2d'), viewport });
        });
    });
</script>
</body>
</html>
