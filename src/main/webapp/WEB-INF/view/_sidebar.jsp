<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- ══ TOPBAR ══ --%>
<header class="topbar">
    <a class="topbar-brand" href="${pageContext.request.contextPath}/dashboard">
        <i class="bi bi-cloud-fill"></i>
        <span><spring:message code="app.name"/></span>
    </a>
    <div class="search-wrap d-none d-md-block">
        <input type="search" class="search-input" placeholder="<spring:message code="dashboard.search.placeholder"/>">
    </div>
</header>

<%-- ══ SIDEBAR ══ --%>
<aside class="sidebar">
    <button class="btn-new" data-bs-toggle="modal" data-bs-target="#newMenuModal">
        <i class="bi bi-plus-lg" style="color:var(--accent)"></i>
        <spring:message code="dashboard.new"/>
    </button>

    <nav>
        <a class="nav-item-link ${activeNav == 'home' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/dashboard">
            <i class="bi bi-house-door${activeNav == 'home' ? '-fill' : ''}"></i>
            <spring:message code="dashboard.menu.home"/>
        </a>
        <a class="nav-item-link ${activeNav == 'starred' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/starred">
            <i class="bi bi-star${activeNav == 'starred' ? '-fill' : ''}"></i>
            <spring:message code="dashboard.menu.starred"/>
        </a>
        <a class="nav-item-link" href="#">
            <i class="bi bi-clock-history"></i>
            <spring:message code="dashboard.menu.recent"/>
        </a>
        <a class="nav-item-link ${activeNav == 'trash' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/trash">
            <i class="bi bi-trash3${activeNav == 'trash' ? '-fill' : ''}"></i>
            <spring:message code="dashboard.menu.trash"/>
        </a>
    </nav>

    <c:if test="${user != null}">
        <div class="storage-box">
            <div class="storage-label"><spring:message code="dashboard.storage.title"/></div>
            <c:set var="pct" value="${(user.usedBytes / user.uploadLimitBytes) * 100}"/>
            <div class="progress mb-2">
                <div class="progress-bar" style="width:${pct}%"></div>
            </div>
            <div class="storage-label">
                <fmt:formatNumber var="usedMb" value="${user.usedBytes / 1048576}" maxFractionDigits="1"/>
                <fmt:formatNumber var="totalMb" value="${user.uploadLimitBytes / 1048576}" maxFractionDigits="0"/>
                <spring:message code="dashboard.storage.using" arguments="${usedMb},${totalMb}"/>
            </div>
        </div>
    </c:if>
</aside>

<%-- ══ NEW MENU MODAL ══ --%>
<div class="modal fade new-menu-modal" id="newMenuModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <button class="new-menu-item" onclick="switchModal('newMenuModal','newFolderModal')">
                <i class="bi bi-folder-plus"></i>
                <spring:message code="dashboard.new.folder"/>
            </button>
            <div class="new-menu-divider"></div>
            <button class="new-menu-item" onclick="switchModal('newMenuModal','uploadFileModal')">
                <i class="bi bi-file-earmark-arrow-up"></i>
                <spring:message code="dashboard.new.fileUpload"/>
            </button>
            <button class="new-menu-item" onclick="switchModal('newMenuModal','uploadFolderModal')">
                <i class="bi bi-folder2-open"></i>
                <spring:message code="dashboard.new.folderUpload"/>
            </button>
        </div>
    </div>
</div>

<%-- ══ NEW FOLDER MODAL ══ --%>
<div class="modal fade" id="newFolderModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/folder/create" method="post">
                <input type="hidden" name="parentId" value="${currentFolder.id}">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="bi bi-folder-plus me-2" style="color:var(--accent)"></i>
                        <spring:message code="dashboard.modal.newFolder.title"/>
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <label class="form-label small text-secondary" for="folderNameInput">
                        <spring:message code="dashboard.modal.newFolder.name"/>
                    </label>
                    <input type="text" class="form-control" id="folderNameInput" name="name"
                           placeholder="Klasör adı" required autofocus>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">
                        <spring:message code="common.cancel"/>
                    </button>
                    <button type="submit" class="btn btn-primary px-4">
                        <spring:message code="dashboard.modal.newFolder.create"/>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<%-- ══ UPLOAD FILE MODAL ══ --%>
<div class="modal fade" id="uploadFileModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/file/upload" method="post" enctype="multipart/form-data">
                <input type="hidden" name="folderId" value="${currentFolder.id}">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="bi bi-file-earmark-arrow-up me-2" style="color:var(--accent)"></i>
                        <spring:message code="dashboard.modal.uploadFile.title"/>
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <label class="form-label small text-secondary" for="fileInput">
                        <spring:message code="dashboard.modal.uploadFile.select"/>
                    </label>
                    <input type="file" class="form-control" id="fileInput" name="file" required>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">
                        <spring:message code="common.cancel"/>
                    </button>
                    <button type="submit" class="btn btn-primary px-4">
                        <spring:message code="dashboard.modal.uploadFile.upload"/>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<%-- ══ UPLOAD FOLDER MODAL ══ --%>
<div class="modal fade" id="uploadFolderModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form id="uploadFolderForm" action="${pageContext.request.contextPath}/folder/upload"
                  method="post" enctype="multipart/form-data">
                <input type="hidden" name="parentId" value="${currentFolder.id}">
                <div id="folderRelativePaths"></div>
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="bi bi-folder2-open me-2" style="color:var(--accent)"></i>
                        <spring:message code="dashboard.modal.uploadFolder.title"/>
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <label class="form-label small text-secondary" for="folderInput">
                        <spring:message code="dashboard.modal.uploadFolder.select"/>
                    </label>
                    <input type="file" class="form-control" id="folderInput" name="files"
                           webkitdirectory directory multiple required>
                    <div id="folderFileCount" class="form-text mt-1"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">
                        <spring:message code="common.cancel"/>
                    </button>
                    <button type="submit" class="btn btn-primary px-4">
                        <spring:message code="dashboard.modal.uploadFolder.upload"/>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    function switchModal(hideId, showId) {
        bootstrap.Modal.getInstance(document.getElementById(hideId)).hide();
        document.getElementById(hideId).addEventListener('hidden.bs.modal', function onHidden() {
            this.removeEventListener('hidden.bs.modal', onHidden);
            bootstrap.Modal.getOrCreateInstance(document.getElementById(showId)).show();
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.getElementById('folderInput').addEventListener('change', function () {
            var container = document.getElementById('folderRelativePaths');
            container.innerHTML = '';
            var count = this.files.length;
            for (var i = 0; i < count; i++) {
                var input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'relativePaths';
                input.value = this.files[i].webkitRelativePath;
                container.appendChild(input);
            }
            document.getElementById('folderFileCount').textContent = count + ' dosya seçildi';
        });
    });
</script>
