<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="tr">
<head>
    <title><spring:message code="trash.title"/> - <spring:message code="app.name"/></title>
    <%@ include file="_layout_head.jsp" %>
</head>
<body>

<c:set var="activeNav" value="trash" scope="request"/>
<%@ include file="_sidebar.jsp" %>

<%-- ══ MAIN ══ --%>
<main class="main">
    <div class="breadcrumb-area mb-4">
        <h4 class="mb-0"><spring:message code="trash.title"/></h4>
    </div>

    <%-- Folders section --%>
    <c:if test="${not empty folders}">
        <div class="section-title"><spring:message code="dashboard.section.folders"/></div>
        <div class="folder-grid">
            <c:forEach var="folder" items="${folders}">
                <div class="folder-card">
                    <i class="bi bi-folder-fill folder-icon"></i>
                    <span class="folder-name">${folder.name}</span>
                    <button class="card-menu-btn" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-1">
                        <li>
                            <a class="dropdown-item small py-2" href="#"
                               onclick="openTrashActionModal('restore', 'folder', '${folder.id}', '${folder.name}')">
                                <i class="bi bi-arrow-counterclockwise me-2"></i><spring:message code="common.restore"/>
                            </a>
                        </li>
                        <li>
                            <a class="dropdown-item small py-2 text-danger" href="#"
                               onclick="openTrashActionModal('delete', 'folder', '${folder.id}', '${folder.name}')">
                                <i class="bi bi-trash3 me-2"></i><spring:message code="common.deletePermanent"/>
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
                        <c:otherwise><i class="bi bi-file-earmark folder-icon"></i></c:otherwise>
                    </c:choose>
                    <span class="folder-name">${file.originalName}</span>
                    <button class="card-menu-btn" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-1">
                        <li>
                            <a class="dropdown-item small py-2" href="#"
                               onclick="openTrashActionModal('restore', 'file', '${file.id}', '${file.originalName}')">
                                <i class="bi bi-arrow-counterclockwise me-2"></i><spring:message code="common.restore"/>
                            </a>
                        </li>
                        <li>
                            <a class="dropdown-item small py-2 text-danger" href="#"
                               onclick="openTrashActionModal('delete', 'file', '${file.id}', '${file.originalName}')">
                                <i class="bi bi-trash3 me-2"></i><spring:message code="common.deletePermanent"/>
                            </a>
                        </li>
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

<%-- ══ TRASH ACTION MODAL ══ --%>
<div class="modal fade" id="trashActionModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered" style="max-width: 400px;">
        <div class="modal-content">
            <form id="trashActionForm" method="post">
                <input type="hidden" id="trashItemId" name="">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title w-100 text-center">
                        <i id="trashActionIcon" class="bi d-block mb-2" style="font-size: 2rem;"></i>
                        <span id="trashActionTitle"></span>
                    </h5>
                </div>
                <div class="modal-body text-center pt-2">
                    <p class="mb-0 text-secondary" id="trashActionMessage"></p>
                    <p class="fw-bold mt-2" id="trashActionItemName"></p>
                </div>
                <div class="modal-footer border-0 justify-content-center pb-4">
                    <button type="button" class="btn btn-light px-4" data-bs-dismiss="modal">
                        <spring:message code="common.cancel"/>
                    </button>
                    <button type="submit" id="trashActionButton" class="btn px-4"></button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function openTrashActionModal(action, type, id, name) {
        const modal = new bootstrap.Modal(document.getElementById('trashActionModal'));
        const form = document.getElementById('trashActionForm');
        const idInput = document.getElementById('trashItemId');
        const title = document.getElementById('trashActionTitle');
        const message = document.getElementById('trashActionMessage');
        const itemName = document.getElementById('trashActionItemName');
        const icon = document.getElementById('trashActionIcon');
        const button = document.getElementById('trashActionButton');

        const controller = type === 'folder' ? 'folder' : 'file';
        idInput.name = type === 'folder' ? 'folderId' : 'fileId';
        idInput.value = id;
        itemName.innerText = name;

        if (action === 'restore') {
            form.action = '${pageContext.request.contextPath}/' + controller + '/restore';
            title.innerText = '<spring:message code="trash.modal.restore.title"/>';
            message.innerText = '<spring:message code="trash.modal.restore.message"/>';
            icon.className = 'bi bi-arrow-counterclockwise text-primary d-block mb-2';
            button.className = 'btn btn-primary px-4';
            button.innerText = '<spring:message code="common.restore"/>';
        } else {
            form.action = '${pageContext.request.contextPath}/' + controller + '/delete-permanent';
            title.innerText = '<spring:message code="trash.modal.deletePermanent.title"/>';
            message.innerText = '<spring:message code="trash.modal.deletePermanent.message"/>';
            icon.className = 'bi bi-exclamation-triangle-fill text-danger d-block mb-2';
            button.className = 'btn btn-danger px-4';
            button.innerText = '<spring:message code="common.deletePermanent"/>';
        }

        modal.show();
    }
</script>
</body>
</html>
