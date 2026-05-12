<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <title><spring:message code="app.name"/></title>
    <%@ include file="_layout_head.jsp" %>
</head>
<body>

<c:set var="activeNav" value="home" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<%-- ══ MAIN ══ --%>
<main class="main">

    <%-- Breadcrumb --%>
    <div class="breadcrumb-area">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item">
                    <a href="${pageContext.request.contextPath}/dashboard">
                        <spring:message code="dashboard.breadcrumb.home"/>
                    </a>
                </li>
                <c:forEach var="pathFolder" items="${folderPath}">
                    <c:choose>
                        <c:when test="${pathFolder.id == currentFolder.id}">
                            <li class="breadcrumb-item active">${pathFolder.name}</li>
                        </c:when>
                        <c:otherwise>
                            <li class="breadcrumb-item">
                                <a href="${pageContext.request.contextPath}/dashboard?folderId=${pathFolder.id}">
                                    ${pathFolder.name}
                                </a>
                            </li>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </ol>
        </nav>
    </div>

    <%-- Alerts --%>
    <c:if test="${not empty error or param.error != null or not empty success}">
        <div class="alert ${not empty success ? 'alert-success' : 'alert-danger'} alert-dismissible fade show mb-4 shadow-sm border-0" role="alert" style="border-radius: 12px; background-color: ${not empty success ? '#e6f4ea' : '#fce8e6'}; color: ${not empty success ? '#137333' : '#c5221f'};">
            <i class="bi ${not empty success ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'} me-2"></i>
            <c:choose>
                <c:when test="${param.error == 'quota'}"><spring:message code="error.quota"/></c:when>
                <c:when test="${param.error == 'maxSize'}"><spring:message code="error.maxSize"/></c:when>
                <c:when test="${param.error == 'invalid'}"><spring:message code="error.invalid"/></c:when>
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
                <div class="folder-card"
                   draggable="true"
                   ondragstart="handleDragStart(event, 'folder', '${folder.id}')"
                   ondragover="handleDragOver(event, this)"
                   ondragleave="handleDragLeave(this)"
                   ondrop="handleDrop(event, this, '${folder.id}')"
                   onclick="location.href='${pageContext.request.contextPath}/dashboard?folderId=${folder.id}'">
                    <i class="bi bi-folder-fill folder-icon"></i>
                    <span class="folder-name">${folder.name}</span>
                    <c:if test="${folder.starred}">
                        <i class="bi bi-star-fill text-warning ms-auto" style="font-size: 0.9rem;"></i>
                    </c:if>
                    <button class="card-menu-btn"
                            data-bs-toggle="dropdown"
                            aria-expanded="false"
                            onclick="event.stopPropagation();">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-1">
                        <li>
                            <form action="${pageContext.request.contextPath}/folder/star" method="post" class="m-0">
                                <input type="hidden" name="folderId" value="${folder.id}">
                                <input type="hidden" name="parentId" value="${currentFolder.id}">
                                <button type="submit" class="dropdown-item small py-2">
                                    <c:choose>
                                        <c:when test="${folder.starred}"><i class="bi bi-star-fill text-warning me-2"></i></c:when>
                                        <c:otherwise><i class="bi bi-star me-2"></i></c:otherwise>
                                    </c:choose>
                                    <c:choose>
                                        <c:when test="${folder.starred}"><spring:message code="common.unstar"/></c:when>
                                        <c:otherwise><spring:message code="common.star"/></c:otherwise>
                                    </c:choose>
                                </button>
                            </form>
                        </li>
                        <li>
                            <a class="dropdown-item small py-2" href="#"
                               onclick="event.preventDefault(); event.stopPropagation(); openRenameModal('folder', '${folder.id}', '${folder.name}')">
                                <i class="bi bi-pencil me-2"></i><spring:message code="common.rename"/>
                            </a>
                        </li>
                        <li>
                            <a class="dropdown-item small py-2 text-danger" href="#"
                               onclick="event.preventDefault(); event.stopPropagation(); openDeleteModal('folder', '${folder.id}', '${folder.name}')">
                                <i class="bi bi-trash3 me-2"></i><spring:message code="common.delete"/>
                            </a>
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
                <c:set var="isVideo" value="${fn:startsWith(file.mimeType, 'video/')}"/>
                <c:set var="isAudio" value="${fn:startsWith(file.mimeType, 'audio/')}"/>
                <c:set var="canPrev" value="${isImage or isPdf or isVideo or isAudio}"/>

                <div class="folder-card ${canPrev ? 'has-thumb' : ''}"
                     draggable="true"
                     ondragstart="handleDragStart(event, 'file', '${file.id}')"
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
                                <c:when test="${isVideo}">
                                    <div class="media-thumb-placeholder video-placeholder">
                                        <i class="bi bi-play-circle-fill"></i>
                                    </div>
                                </c:when>
                                <c:when test="${isAudio}">
                                    <div class="media-thumb-placeholder audio-placeholder">
                                        <i class="bi bi-music-note-beamed"></i>
                                    </div>
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
                            <c:when test="${isVideo}"><i class="bi bi-file-play folder-icon" style="color:#1a73e8;"></i></c:when>
                            <c:when test="${isAudio}"><i class="bi bi-file-music folder-icon" style="color:#9c27b0;"></i></c:when>
                            <c:when test="${fn:contains(file.mimeType, 'zip') or fn:contains(file.mimeType, 'compressed')}"><i class="bi bi-file-zip folder-icon"></i></c:when>
                            <c:when test="${fn:contains(file.mimeType, 'word')}"><i class="bi bi-file-word folder-icon"></i></c:when>
                            <c:when test="${fn:contains(file.mimeType, 'sheet') or fn:contains(file.mimeType, 'excel')}"><i class="bi bi-file-excel folder-icon"></i></c:when>
                            <c:otherwise><i class="bi bi-file-earmark folder-icon"></i></c:otherwise>
                        </c:choose>
                        <span class="folder-name" title="${fn:escapeXml(file.originalName)}">${fn:escapeXml(file.originalName)}</span>
                        <c:if test="${file.starred}">
                            <i class="bi bi-star-fill text-warning" style="font-size:0.9rem;"></i>
                        </c:if>
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
                                    <input type="hidden" name="folderId" value="${currentFolder.id}">
                                    <button type="submit" class="dropdown-item small py-2" onclick="event.stopPropagation()">
                                        <c:choose>
                                            <c:when test="${file.starred}"><i class="bi bi-star-fill text-warning me-2"></i></c:when>
                                            <c:otherwise><i class="bi bi-star me-2"></i></c:otherwise>
                                        </c:choose>
                                        <c:choose>
                                            <c:when test="${file.starred}"><spring:message code="common.unstar"/></c:when>
                                            <c:otherwise><spring:message code="common.star"/></c:otherwise>
                                        </c:choose>
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

    <%-- Empty state --%>
    <c:if test="${empty folders and empty files}">
        <div class="empty-state">
            <i class="bi bi-folder2-open"></i>
            <p><spring:message code="dashboard.empty"/></p>
        </div>
    </c:if>

</main>

<%-- ══ RENAME MODAL ══ --%>
<div class="modal fade" id="renameModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form id="renameForm" method="post">
                <input type="hidden" id="renameRedirectId" name="parentId" value="${currentFolder.id}">
                <input type="hidden" id="renameItemId" name="">
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
                        <span><spring:message code="dashboard.modal.delete.title"/></span>
                    </h5>
                </div>
                <div class="modal-body text-center pt-2">
                    <p class="mb-0 text-secondary" id="deleteModalMessage"><spring:message code="dashboard.modal.delete.message"/></p>
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
                        <i class="bi bi-download me-1"></i><spring:message code="common.download"/>
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
    /* ── Drag & Drop ── */
    function handleDragStart(e, type, id) {
        e.dataTransfer.setData("type", type);
        e.dataTransfer.setData("id", id);
        e.dataTransfer.effectAllowed = "move";
        // Sürüklenen öğeyi biraz şeffaf yap
        setTimeout(() => e.target.style.opacity = "0.5", 0);
    }

    document.addEventListener("dragend", (e) => {
        if (e.target.classList && e.target.classList.contains('folder-card')) {
            e.target.style.opacity = "1";
        }
    });

    function handleDragOver(e, el) {
        e.preventDefault();
        e.dataTransfer.dropEffect = "move";
        el.classList.add('drag-over');
    }

    function handleDragLeave(el) {
        el.classList.remove('drag-over');
    }

    function handleDrop(e, el, targetFolderId) {
        e.preventDefault();
        el.classList.remove('drag-over');

        const type = e.dataTransfer.getData("type");
        const id = e.dataTransfer.getData("id");

        if (!type || !id) return;
        if (type === 'folder' && id === targetFolderId) return;

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = type === 'folder' 
            ? '${pageContext.request.contextPath}/folder/move' 
            : '${pageContext.request.contextPath}/file/move';

        const idParam = document.createElement('input');
        idParam.type = 'hidden';
        idParam.name = type === 'folder' ? 'folderId' : 'fileId';
        idParam.value = id;
        form.appendChild(idParam);

        const targetParam = document.createElement('input');
        targetParam.type = 'hidden';
        targetParam.name = 'targetFolderId';
        targetParam.value = targetFolderId;
        form.appendChild(targetParam);

        const sourceParam = document.createElement('input');
        sourceParam.type = 'hidden';
        sourceParam.name = 'sourceFolderId';
        sourceParam.value = '${currentFolder.id}';
        form.appendChild(sourceParam);

        document.body.appendChild(form);
        form.submit();
    }

    function switchModal(hideId, showId) {
        const hideEl = document.getElementById(hideId);
        const hideModal = bootstrap.Modal.getOrCreateInstance(hideEl);
        hideModal.hide();
        
        hideEl.addEventListener('hidden.bs.modal', function onHidden() {
            this.removeEventListener('hidden.bs.modal', onHidden);
            const showModal = bootstrap.Modal.getOrCreateInstance(document.getElementById(showId));
            showModal.show();
        });
    }

    function openRenameModal(type, id, currentName) {
        const modalEl = document.getElementById('renameModal');
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        const form = document.getElementById('renameForm');
        const input = document.getElementById('renameInput');
        const idInput = document.getElementById('renameItemId');
        const redirectInput = document.getElementById('renameRedirectId');
        const currentFolderId = '${currentFolder.id}';

        if (type === 'folder') {
            form.action = '${pageContext.request.contextPath}/folder/rename';
            idInput.name = 'folderId';
            redirectInput.name = 'parentId';
        } else {
            form.action = '${pageContext.request.contextPath}/file/rename';
            idInput.name = 'fileId';
            redirectInput.name = 'folderId';
        }

        idInput.value = id;
        redirectInput.value = currentFolderId;
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
            messageDisplay.innerText = '<spring:message code="dashboard.modal.delete.folderMessage"/>';
        } else {
            form.action = '${pageContext.request.contextPath}/file/delete';
            idInput.name = 'fileId';
            messageDisplay.innerText = '<spring:message code="dashboard.modal.delete.fileMessage"/>';
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
            img.onerror = () => { body.innerHTML = '<div class="text-center text-secondary p-5"><i class="bi bi-exclamation-triangle" style="font-size:3rem;"></i><p class="mt-3"><spring:message code="dashboard.preview.error"/></p></div>'; };
            img.src = previewUrl;
        } else if (mimeType === 'application/pdf') {
            body.innerHTML = '<iframe class="preview-pdf-frame" src="' + previewUrl + '" title="' + fileName + '"></iframe>';
        } else if (mimeType.startsWith('video/')) {
            body.innerHTML = '<video class="preview-media" controls autoplay>' +
                '<source src="' + previewUrl + '" type="' + mimeType + '">' +
                '</video>';
        } else if (mimeType.startsWith('audio/')) {
            body.innerHTML = '<div class="d-flex flex-column align-items-center justify-content-center gap-4" style="padding:48px 40px;">' +
                '<i class="bi bi-music-note-beamed" style="font-size:5rem; color:#9c27b0;"></i>' +
                '<p class="text-white mb-0 text-center" style="font-size:0.95rem; opacity:0.8; max-width:380px; word-break:break-all;">' + fileName + '</p>' +
                '<audio class="preview-audio" controls autoplay>' +
                '<source src="' + previewUrl + '" type="' + mimeType + '">' +
                '</audio></div>';
        }
    }

    document.getElementById('previewModal').addEventListener('hidden.bs.modal', function () {
        const body = document.getElementById('previewModalBody');
        const video = body.querySelector('video');
        const audio = body.querySelector('audio');
        if (video) video.pause();
        if (audio) audio.pause();
        body.innerHTML = '';
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
