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
                            <c:when test="${fn:startsWith(file.mimeType, 'video/')}"><i class="bi bi-file-play folder-icon"></i></c:when>
                            <c:when test="${fn:startsWith(file.mimeType, 'audio/')}"><i class="bi bi-file-music folder-icon"></i></c:when>
                            <c:when test="${fn:contains(file.mimeType, 'zip') or fn:contains(file.mimeType, 'compressed')}"><i class="bi bi-file-zip folder-icon"></i></c:when>
                            <c:when test="${fn:contains(file.mimeType, 'word')}"><i class="bi bi-file-word folder-icon"></i></c:when>
                            <c:when test="${fn:contains(file.mimeType, 'sheet') or fn:contains(file.mimeType, 'excel')}"><i class="bi bi-file-excel folder-icon"></i></c:when>
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
                                        <i class="bi bi-eye me-2"></i>Önizle
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
                            <li>
                                <a class="dropdown-item small py-2" href="#"
                                   onclick="event.preventDefault(); event.stopPropagation(); openRenameModal('file', '${file.id}', '${fn:escapeXml(file.originalName)}')">
                                    <i class="bi bi-pencil me-2"></i><spring:message code="common.rename"/>
                                </a>
                            </li>
                            <li><hr class="dropdown-divider my-1"></li>
                            <li>
                                <a class="dropdown-item small py-2 text-danger" href="#"
                                   onclick="event.preventDefault(); event.stopPropagation(); openDeleteModal('file', '${file.id}', '${fn:escapeXml(file.originalName)}')">
                                    <i class="bi bi-trash3 me-2"></i><spring:message code="common.delete"/>
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

<%-- ══ RENAME MODAL ══ --%>
<div class="modal fade" id="renameModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form id="renameForm" method="post">
                <input type="hidden" id="renameItemId" name="">
                <input type="hidden" id="renameRedirectId" name="">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="bi bi-pencil me-2" style="color:var(--accent)"></i>
                        <spring:message code="dashboard.modal.rename.title"/>
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <label class="form-label small text-secondary" for="renameInput">
                        <spring:message code="dashboard.modal.rename.newName"/>
                    </label>
                    <input type="text" class="form-control" id="renameInput" name="newName"
                           required autofocus>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">
                        <spring:message code="common.cancel"/>
                    </button>
                    <button type="submit" class="btn btn-primary px-4">
                        <spring:message code="common.rename"/>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<%-- ══ DELETE CONFIRMATION MODAL ══ --%>
<div class="modal fade" id="deleteConfirmModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered" style="max-width: 400px;">
        <div class="modal-content">
            <form id="deleteForm" method="post">
                <input type="hidden" id="deleteItemId" name="">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title w-100 text-center">
                        <i class="bi bi-exclamation-triangle-fill text-danger d-block mb-2" style="font-size: 2rem;"></i>
                        <span>Silme Onayı</span>
                    </h5>
                </div>
                <div class="modal-body text-center pt-2">
                    <p class="mb-0 text-secondary" id="deleteModalMessage">Bu öğeyi silmek istediğinize emin misiniz?</p>
                    <p class="fw-bold mt-2" id="deleteItemNameDisplay"></p>
                </div>
                <div class="modal-footer border-0 justify-content-center pb-4">
                    <button type="button" class="btn btn-light px-4" data-bs-dismiss="modal">
                        <spring:message code="common.cancel"/>
                    </button>
                    <button type="submit" class="btn btn-danger px-4">
                        <spring:message code="common.delete"/>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<%-- ══ FILE PREVIEW MODAL ══ --%>
<div class="modal fade" id="previewModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content" style="border-radius:12px; overflow:hidden;">
            <div class="modal-header" style="background:#2d2d2d; border-bottom:1px solid #3c4043; padding:12px 20px;">
                <h6 class="modal-title text-white mb-0" id="previewModalTitle"
                    style="font-weight:500; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; max-width:600px;"></h6>
                <div class="d-flex gap-2 ms-auto">
                    <a id="previewDownloadBtn" href="#" class="btn btn-sm btn-outline-light" style="font-size:0.8rem;">
                        <i class="bi bi-download me-1"></i>İndir
                    </a>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
            </div>
            <div class="modal-body" id="previewModalBody"></div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function openRenameModal(type, id, currentName) {
        const modalEl = document.getElementById('renameModal');
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        const form = document.getElementById('renameForm');
        const input = document.getElementById('renameInput');
        const idInput = document.getElementById('renameItemId');
        const redirectInput = document.getElementById('renameRedirectId');

        if (type === 'folder') {
            form.action = '${pageContext.request.contextPath}/folder/rename';
            idInput.name = 'folderId';
            redirectInput.name = 'parentId';
            redirectInput.value = '';
        } else {
            form.action = '${pageContext.request.contextPath}/file/rename';
            idInput.name = 'fileId';
            redirectInput.name = 'redirect';
            redirectInput.value = 'starred';
        }

        idInput.value = id;
        input.value = currentName;
        modal.show();

        modalEl.addEventListener('shown.bs.modal', function () {
            input.focus();
            input.select();
        }, { once: true });
    }

    function openDeleteModal(type, id, name) {
        const modalEl = document.getElementById('deleteConfirmModal');
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        const form = document.getElementById('deleteForm');
        const idInput = document.getElementById('deleteItemId');
        const nameDisplay = document.getElementById('deleteItemNameDisplay');
        const messageDisplay = document.getElementById('deleteModalMessage');

        if (type === 'folder') {
            form.action = '${pageContext.request.contextPath}/folder/delete';
            idInput.name = 'folderId';
            messageDisplay.innerText = 'Bu klasörü ve içeriğini silmek istediğinize emin misiniz?';
        } else {
            form.action = '${pageContext.request.contextPath}/file/delete';
            idInput.name = 'fileId';
            messageDisplay.innerText = 'Bu dosyayı silmek istediğinize emin misiniz?';
        }

        idInput.value = id;
        nameDisplay.innerText = name;
        modal.show();
    }

    function openPreviewModal(fileId, fileName, mimeType) {
        const modalEl = document.getElementById('previewModal');
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        const body = document.getElementById('previewModalBody');
        const title = document.getElementById('previewModalTitle');
        const downloadBtn = document.getElementById('previewDownloadBtn');

        const ctx = '${pageContext.request.contextPath}';
        const previewUrl = ctx + '/file/preview?fileId=' + fileId;
        const downloadUrl = ctx + '/file/download?fileId=' + fileId;

        title.textContent = fileName;
        downloadBtn.href = downloadUrl;
        body.innerHTML = '<div class="d-flex align-items-center justify-content-center" style="min-height:300px;">' +
            '<div class="spinner-border text-light" role="status"></div></div>';
        modal.show();

        if (mimeType.startsWith('image/')) {
            const img = document.createElement('img');
            img.className = 'preview-img';
            img.alt = fileName;
            img.onload = () => { body.innerHTML = ''; body.appendChild(img); };
            img.onerror = () => { body.innerHTML = '<div class="text-center text-secondary p-5"><i class="bi bi-exclamation-triangle" style="font-size:3rem;"></i><p class="mt-3">Önizleme yüklenemedi.</p></div>'; };
            img.src = previewUrl;
        } else if (mimeType === 'application/pdf') {
            body.innerHTML = '<iframe class="preview-pdf-frame" src="' + previewUrl + '" title="' + fileName + '"></iframe>';
        }
    }

    document.getElementById('previewModal').addEventListener('hidden.bs.modal', function () {
        document.getElementById('previewModalBody').innerHTML = '';
    });
</script>
<script type="module">
    import { getDocument, GlobalWorkerOptions } from 'https://cdn.jsdelivr.net/npm/pdfjs-dist@4.4.168/build/pdf.min.mjs';
    GlobalWorkerOptions.workerSrc = 'https://cdn.jsdelivr.net/npm/pdfjs-dist@4.4.168/build/pdf.worker.min.mjs';

    document.querySelectorAll('canvas.pdf-thumb-canvas').forEach(canvas => {
        const url = canvas.dataset.pdfUrl;
        getDocument(url).promise
            .then(pdf => pdf.getPage(1))
            .then(page => {
                const wrap = canvas.parentElement;
                const viewport = page.getViewport({ scale: wrap.clientWidth / page.getViewport({ scale: 1 }).width });
                canvas.width = viewport.width;
                canvas.height = viewport.height;
                canvas.style.cssText = 'width:100%;height:100%;object-fit:cover;';
                page.render({ canvasContext: canvas.getContext('2d'), viewport });
            })
            .catch(() => {
                const icon = document.createElement('i');
                icon.className = 'bi bi-file-pdf thumb-icon';
                icon.style.color = '#ea4335';
                canvas.replaceWith(icon);
            });
    });
</script>
</body>
</html>
