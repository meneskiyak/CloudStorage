<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <title><spring:message code="trash.title"/> - CloudStorage</title>
    <%@ include file="_layout_head.jsp" %>
</head>
<body>

<c:set var="activeNav" value="trash" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<main class="main">
    <%-- Breadcrumb --%>
    <div class="breadcrumb-area mb-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-0">
                <li class="breadcrumb-item">
                    <a href="${pageContext.request.contextPath}/trash" class="text-decoration-none">
                        <i class="bi bi-trash3 me-1"></i><spring:message code="trash.title"/>
                    </a>
                </li>
                <c:forEach var="pathFolder" items="${folderPath}">
                    <c:choose>
                        <c:when test="${pathFolder.id == currentFolder.id}">
                            <li class="breadcrumb-item active">${pathFolder.name}</li>
                        </c:when>
                        <c:otherwise>
                            <li class="breadcrumb-item">
                                <a href="${pageContext.request.contextPath}/trash?folderId=${pathFolder.id}" class="text-decoration-none">
                                    ${pathFolder.name}
                                </a>
                            </li>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </ol>
        </nav>
    </div>

    <%-- 30 gün uyarı banner --%>
    <c:if test="${not empty folders or not empty files}">
        <div class="alert alert-warning d-flex align-items-center gap-2 mb-4 py-2 px-3" style="font-size:0.85rem; border-radius:8px;">
            <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
            <span>Çöp kutusundaki öğeler <strong>30 gün</strong> sonra otomatik olarak kalıcı silinir.</span>
        </div>
    </c:if>

    <%-- Folders section --%>
    <c:if test="${not empty folders}">
        <div class="section-title"><spring:message code="dashboard.section.folders"/></div>
        <div class="folder-grid">
            <c:forEach var="folder" items="${folders}">
                <div class="folder-card" onclick="location.href='${pageContext.request.contextPath}/trash?folderId=${folder.id}'" data-deleted-at="${folder.deletedAt.time}">
                    <i class="bi bi-folder-fill folder-icon"></i>
                    <span class="folder-name">${folder.name}</span>
                    <span class="days-left-badge"></span>
                    <button class="card-menu-btn" data-bs-toggle="dropdown" onclick="event.stopPropagation()">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0">
                        <li><a class="dropdown-item small" href="#" onclick="event.stopPropagation(); openTrashActionModal('restore', 'folder', '${folder.id}', '${folder.name}')"><i class="bi bi-arrow-counterclockwise me-2"></i><spring:message code="common.restore"/></a></li>
                        <li><a class="dropdown-item small text-danger" href="#" onclick="event.stopPropagation(); openTrashActionModal('delete', 'folder', '${folder.id}', '${folder.name}')"><i class="bi bi-trash3 me-2"></i><spring:message code="common.deletePermanent"/></a></li>
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
                     onclick="${canPrev ? 'openPreviewModal('.concat(file.id).concat(',\'').concat(fn:escapeXml(file.originalName)).concat('\',\'').concat(file.mimeType).concat('\')') : 'void(0)'}"
                     data-deleted-at="${file.deletedAt.time}">

                    <c:if test="${canPrev}">
                        <div class="file-thumb-wrap">
                            <c:choose>
                                <c:when test="${isImage}">
                                    <img src="${pageContext.request.contextPath}/file/preview?fileId=${file.id}" alt="${file.originalName}" loading="lazy">
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

                    <div class="${canPrev ? 'card-meta-row' : 'd-flex align-items-center gap-2 flex-grow-1'}">
                        <c:choose>
                            <c:when test="${isImage}"><i class="bi bi-file-image folder-icon"></i></c:when>
                            <c:when test="${isPdf}"><i class="bi bi-file-pdf folder-icon" style="color:#ea4335;"></i></c:when>
                            <c:when test="${isVideo}"><i class="bi bi-file-play folder-icon" style="color:#1a73e8;"></i></c:when>
                            <c:when test="${isAudio}"><i class="bi bi-file-music folder-icon" style="color:#9c27b0;"></i></c:when>
                            <c:otherwise><i class="bi bi-file-earmark folder-icon"></i></c:otherwise>
                        </c:choose>
                        <span class="folder-name">${file.originalName}</span>
                    </div>
                    <span class="days-left-badge"></span>

                    <button class="card-menu-btn" data-bs-toggle="dropdown" onclick="event.stopPropagation()">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0">
                        <li><a class="dropdown-item small" href="#" onclick="event.stopPropagation(); openTrashActionModal('restore', 'file', '${file.id}', '${file.originalName}')"><i class="bi bi-arrow-counterclockwise me-2"></i><spring:message code="common.restore"/></a></li>
                        <li><a class="dropdown-item small text-danger" href="#" onclick="event.stopPropagation(); openTrashActionModal('delete', 'file', '${file.id}', '${file.originalName}')"><i class="bi bi-trash3 me-2"></i><spring:message code="common.deletePermanent"/></a></li>
                    </ul>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${empty folders and empty files}">
        <div class="empty-state">
            <i class="bi bi-trash3"></i>
            <p><spring:message code="trash.empty"/></p>
        </div>
    </c:if>
</main>

<div class="modal fade" id="previewModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content" style="border-radius:12px; overflow:hidden;">
            <div class="modal-header" style="background:#2d2d2d; border-bottom:1px solid #3c4043; padding:12px 20px;">
                <h6 class="modal-title text-white mb-0" id="previewModalTitle"></h6>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="previewModalBody"></div>
        </div>
    </div>
</div>

<div class="modal fade" id="trashActionModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered" style="max-width: 400px;">
        <div class="modal-content">
            <form id="trashActionForm" method="post">
                <input type="hidden" id="trashItemId" name="">
                <div class="modal-body text-center pt-4">
                    <i id="trashActionIcon" class="bi d-block mb-3" style="font-size: 3rem;"></i>
                    <h5 id="trashActionTitle"></h5>
                    <p id="trashActionMessage" class="text-secondary small"></p>
                    <p id="trashActionItemName" class="fw-bold"></p>
                </div>
                <div class="modal-footer border-0 justify-content-center pb-4">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal"><spring:message code="common.cancel"/></button>
                    <button type="submit" id="trashActionButton" class="btn"></button>
                </div>
            </form>
        </div>
    </div>
</div>

<style>
    .days-left-badge {
        display: none;
        font-size: 0.7rem;
        font-weight: 600;
        padding: 1px 7px;
        border-radius: 20px;
        margin-top: 4px;
        white-space: nowrap;
        align-self: flex-start;
    }
    .days-left-badge.urgent {
        background: #fce8e6;
        color: #c5221f;
    }
    .days-left-badge.warning {
        background: #fef7e0;
        color: #b45309;
    }
    .days-left-badge.ok {
        background: #e6f4ea;
        color: #1e7e34;
    }
</style>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function openPreviewModal(fileId, fileName, mimeType) {
        const modal = new bootstrap.Modal(document.getElementById('previewModal'));
        const body = document.getElementById('previewModalBody');
        const title = document.getElementById('previewModalTitle');
        const previewUrl = '${pageContext.request.contextPath}/file/preview?fileId=' + fileId;

        title.textContent = fileName;
        body.innerHTML = '<div class="text-center p-5"><div class="spinner-border text-primary" role="status"></div></div>';
        modal.show();

        if (mimeType.startsWith('image/')) {
            const img = document.createElement('img');
            img.className = 'preview-img';
            img.src = previewUrl;
            img.onload = () => { body.innerHTML = ''; body.appendChild(img); };
        } else if (mimeType === 'application/pdf') {
            body.innerHTML = '<iframe class="preview-pdf-frame" src="' + previewUrl + '"></iframe>';
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

    function openTrashActionModal(action, type, id, name) {
        const modal = new bootstrap.Modal(document.getElementById('trashActionModal'));
        const form = document.getElementById('trashActionForm');
        const idInput = document.getElementById('trashItemId');
        const title = document.getElementById('trashActionTitle');
        const message = document.getElementById('trashActionMessage');
        const itemName = document.getElementById('trashActionItemName');
        const icon = document.getElementById('trashActionIcon');
        const button = document.getElementById('trashActionButton');

        idInput.name = type === 'folder' ? 'folderId' : 'fileId';
        idInput.value = id;
        itemName.innerText = name;

        if (action === 'restore') {
            form.action = '${pageContext.request.contextPath}/' + type + '/restore';
            title.innerText = '<spring:message code="common.restore"/>';
            message.innerText = '<spring:message code="trash.modal.restore.message"/>';
            icon.className = 'bi bi-arrow-counterclockwise text-primary';
            button.className = 'btn btn-primary';
            button.innerText = '<spring:message code="common.restore"/>';
        } else {
            form.action = '${pageContext.request.contextPath}/' + type + '/delete-permanent';
            title.innerText = '<spring:message code="common.deletePermanent"/>';
            message.innerText = '<spring:message code="trash.modal.deletePermanent.message"/>';
            icon.className = 'bi bi-exclamation-triangle-fill text-danger';
            button.className = 'btn btn-danger';
            button.innerText = '<spring:message code="common.deletePermanent"/>';
        }
        modal.show();
    }

    document.querySelectorAll('.folder-card[data-deleted-at]').forEach(card => {
        const deletedAt = parseInt(card.dataset.deletedAt, 10);
        if (!deletedAt) return;
        const daysLeft = Math.ceil((deletedAt + 30 * 24 * 60 * 60 * 1000 - Date.now()) / (24 * 60 * 60 * 1000));
        const badge = card.querySelector('.days-left-badge');
        if (!badge) return;
        badge.style.display = 'inline-block';
        if (daysLeft <= 3) {
            badge.classList.add('urgent');
            badge.textContent = daysLeft <= 0 ? 'Bugün silinecek' : daysLeft + ' gün kaldı';
        } else if (daysLeft <= 10) {
            badge.classList.add('warning');
            badge.textContent = daysLeft + ' gün kaldı';
        } else {
            badge.classList.add('ok');
            badge.textContent = daysLeft + ' gün kaldı';
        }
    });
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
