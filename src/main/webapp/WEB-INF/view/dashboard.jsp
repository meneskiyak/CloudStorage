<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="tr">
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
                <c:if test="${not empty currentFolder}">
                    <li class="breadcrumb-item active">${currentFolder.name}</li>
                </c:if>
            </ol>
        </nav>
    </div>

    <%-- Folders section --%>
    <c:if test="${not empty folders}">
        <div class="section-title"><spring:message code="dashboard.section.folders"/></div>
        <div class="folder-grid">
            <c:forEach var="folder" items="${folders}">
                <div class="folder-card"
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
                                    <i class="bi bi-star me-2"></i>
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
                    <c:if test="${file.starred}">
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
                            <form action="${pageContext.request.contextPath}/file/star" method="post" class="m-0">
                                <input type="hidden" name="fileId" value="${file.id}">
                                <input type="hidden" name="folderId" value="${currentFolder.id}">
                                <button type="submit" class="dropdown-item small py-2">
                                    <i class="bi bi-star me-2"></i>
                                    <c:choose>
                                        <c:when test="${file.starred}"><spring:message code="common.unstar"/></c:when>
                                        <c:otherwise><spring:message code="common.star"/></c:otherwise>
                                    </c:choose>
                                </button>
                            </form>
                        </li>
                        <li>
                            <a class="dropdown-item small py-2"
                               href="${pageContext.request.contextPath}/file/download?fileId=${file.id}">
                                <i class="bi bi-download me-2"></i><spring:message code="common.download"/>
                            </a>
                        </li>
                        <li>
                            <a class="dropdown-item small py-2" href="#"
                               onclick="event.preventDefault(); event.stopPropagation(); openRenameModal('file', '${file.id}', '${file.originalName}')">
                                <i class="bi bi-pencil me-2"></i><spring:message code="common.rename"/>
                            </a>
                        </li>
                        <li><hr class="dropdown-divider my-1"></li>
                        <li>
                            <a class="dropdown-item small py-2 text-danger" href="#"
                               onclick="event.preventDefault(); event.stopPropagation(); openDeleteModal('file', '${file.id}', '${file.originalName}')">
                                <i class="bi bi-trash3 me-2"></i><spring:message code="common.delete"/>
                            </a>
                        </li>
                    </ul>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <%-- Empty state --%>
    <c:if test="${empty folders and empty files}">
        <div class="empty-state">
            <i class="bi bi-folder2-open"></i>
            <p><spring:message code="dashboard.empty"/></p>
            <button class="btn btn-primary mt-2" data-bs-toggle="modal" data-bs-target="#newMenuModal">
                <i class="bi bi-plus-lg me-1"></i>
                <spring:message code="dashboard.new"/>
            </button>
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
                <input type="hidden" name="folderId" value="${currentFolder.id}">
                <input type="hidden" name="parentId" value="${currentFolder.id}">
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
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
</script>
</body>
</html>
