<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="app.name"/></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        :root {
            --sidebar-w: 256px;
            --topbar-h: 64px;
            --accent: #1a73e8;
            --surface: #f8f9fa;
            --border: #dadce0;
        }

        body {
            background: var(--surface);
            font-family: 'Google Sans', 'Segoe UI', sans-serif;
            overflow-x: hidden;
        }

        /* ── Topbar ── */
        .topbar {
            position: fixed;
            top: 0; left: 0; right: 0;
            height: var(--topbar-h);
            background: #fff;
            border-bottom: 1px solid var(--border);
            z-index: 1030;
            display: flex;
            align-items: center;
            padding: 0 16px;
            gap: 12px;
        }

        .topbar-brand {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 1.2rem;
            font-weight: 600;
            color: #5f6368;
            text-decoration: none;
            min-width: 180px;
        }

        .topbar-brand i { color: var(--accent); font-size: 1.5rem; }

        .search-wrap {
            flex: 1;
            max-width: 720px;
            margin: 0 auto;
        }

        .search-input {
            background: #f1f3f4;
            border: none;
            border-radius: 24px;
            padding: 10px 20px;
            width: 100%;
            font-size: 0.95rem;
            transition: background 0.2s, box-shadow 0.2s;
        }

        .search-input:focus {
            outline: none;
            background: #fff;
            box-shadow: 0 2px 8px rgba(0,0,0,.15);
        }

        .topbar-user {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-left: auto;
        }

        .avatar {
            width: 36px; height: 36px;
            border-radius: 50%;
            background: var(--accent);
            color: #fff;
            font-weight: 600;
            display: flex; align-items: center; justify-content: center;
            font-size: 0.9rem;
        }

        /* ── Sidebar ── */
        .sidebar {
            position: fixed;
            top: var(--topbar-h);
            left: 0;
            width: var(--sidebar-w);
            height: calc(100vh - var(--topbar-h));
            padding: 12px 8px;
            overflow-y: auto;
            z-index: 1020;
        }

        .btn-new {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 14px 20px;
            background: #fff;
            border: none;
            border-radius: 16px;
            box-shadow: 0 1px 3px rgba(0,0,0,.2);
            font-size: 0.95rem;
            font-weight: 500;
            color: #3c4043;
            width: 100%;
            margin-bottom: 8px;
            transition: box-shadow 0.2s;
        }

        .btn-new:hover { box-shadow: 0 4px 8px rgba(0,0,0,.2); background: #f8f9fa; }
        .btn-new i { font-size: 1.1rem; }

        .nav-item-link {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 16px;
            border-radius: 0 24px 24px 0;
            color: #3c4043;
            font-size: 0.9rem;
            font-weight: 500;
            text-decoration: none;
            margin: 2px -8px;
            transition: background 0.15s;
        }

        .nav-item-link i { font-size: 1.15rem; }
        .nav-item-link:hover { background: #e8eaed; color: #3c4043; }
        .nav-item-link.active { background: #d3e3fd; color: var(--accent); }

        .storage-box {
            padding: 16px;
            margin-top: 16px;
        }

        .storage-label { font-size: 0.8rem; color: #5f6368; margin-bottom: 8px; }

        .progress { height: 4px; border-radius: 2px; }
        .progress-bar { background: var(--accent); border-radius: 2px; }

        /* ── Main ── */
        .main {
            margin-left: var(--sidebar-w);
            margin-top: var(--topbar-h);
            padding: 24px 32px;
            min-height: calc(100vh - var(--topbar-h));
        }

        /* ── Breadcrumb ── */
        .breadcrumb-area {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 20px;
            flex-wrap: wrap;
            gap: 12px;
        }

        .breadcrumb { margin: 0; }
        .breadcrumb-item a { color: #3c4043; text-decoration: none; font-weight: 500; }
        .breadcrumb-item a:hover { color: var(--accent); }
        .breadcrumb-item.active { color: #3c4043; font-weight: 500; }
        .breadcrumb-item + .breadcrumb-item::before {
            font-family: "bootstrap-icons";
            content: "\F285";
        }

        /* ── Section heading ── */
        .section-title {
            font-size: 0.85rem;
            font-weight: 600;
            color: #5f6368;
            text-transform: uppercase;
            letter-spacing: .5px;
            margin-bottom: 12px;
        }

        /* ── Folder grid ── */
        .folder-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 12px;
            margin-bottom: 32px;
        }

        .folder-card {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 12px 14px;
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 10px;
            cursor: pointer;
            text-decoration: none;
            color: #3c4043;
            font-size: 0.9rem;
            font-weight: 500;
            position: relative;
            transition: background 0.15s, box-shadow 0.15s;
            overflow: hidden;
        }

        .folder-card:hover { background: #f1f3f4; color: #3c4043; box-shadow: 0 1px 4px rgba(0,0,0,.12); }
        .folder-card i.folder-icon { font-size: 1.4rem; color: #5f6368; flex-shrink: 0; }
        .folder-card .folder-name { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

        .folder-card .card-menu-btn {
            opacity: 0;
            background: none;
            border: none;
            padding: 4px 6px;
            border-radius: 50%;
            color: #5f6368;
            font-size: 1rem;
            transition: opacity 0.15s, background 0.15s;
            flex-shrink: 0;
        }

        .folder-card:hover .card-menu-btn { opacity: 1; }
        .folder-card .card-menu-btn:hover { background: #e8eaed; }

        /* ── Empty state ── */
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #5f6368;
        }

        .empty-state i { font-size: 4rem; color: #dadce0; margin-bottom: 16px; display: block; }
        .empty-state p { font-size: 1rem; }

        /* ── Modal ── */
        .modal-content { border: none; border-radius: 12px; box-shadow: 0 8px 32px rgba(0,0,0,.18); }
        .modal-header { border-bottom: 1px solid #f1f3f4; padding: 20px 24px 16px; }
        .modal-title { font-size: 1rem; font-weight: 600; color: #202124; }
        .modal-body { padding: 16px 24px; }
        .modal-footer { border-top: 1px solid #f1f3f4; padding: 12px 24px 20px; }
        .form-control:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(26,115,232,.15); }

        /* ── New menu modal ── */
        .new-menu-modal .modal-dialog {
            position: fixed;
            top: 120px;
            left: 20px;
            margin: 0;
            width: 240px;
        }

        .new-menu-modal .modal-content {
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0,0,0,.20);
            padding: 6px 0;
        }

        .new-menu-item {
            display: flex;
            align-items: center;
            gap: 14px;
            padding: 10px 20px;
            font-size: 0.9rem;
            color: #3c4043;
            cursor: pointer;
            border: none;
            background: none;
            width: 100%;
            text-align: left;
            transition: background 0.12s;
        }

        .new-menu-item:hover { background: #f1f3f4; }
        .new-menu-item i { font-size: 1.15rem; color: #5f6368; width: 20px; text-align: center; }

        .new-menu-divider { border-top: 1px solid #e0e0e0; margin: 4px 0; }
    </style>
</head>
<body>

<%-- ══ TOPBAR ══ --%>
<header class="topbar">
    <a class="topbar-brand" href="${pageContext.request.contextPath}/dashboard">
        <i class="bi bi-cloud-fill"></i>
        <span><spring:message code="app.name"/></span>
    </a>

    <div class="search-wrap d-none d-md-block">
        <input type="search" class="search-input" placeholder="<spring:message code="dashboard.search.placeholder"/>">
    </div>

<%--    <div class="topbar-user">--%>
<%--        <div class="avatar">${fn:toUpperCase(fn:substring(user.username, 0, 1))}</div>--%>
<%--        <span class="d-none d-lg-block text-secondary" style="font-size:.9rem">${user.username}</span>--%>
<%--        <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm btn-light ms-2">--%>
<%--            <spring:message code="dashboard.logout"/>--%>
<%--        </a>--%>
<%--    </div>--%>
</header>

<%-- ══ SIDEBAR ══ --%>
<aside class="sidebar">
    <%-- New button --%>
    <button class="btn-new" data-bs-toggle="modal" data-bs-target="#newMenuModal">
        <i class="bi bi-plus-lg" style="color:var(--accent)"></i>
        <spring:message code="dashboard.new"/>
    </button>

    <nav>
        <a class="nav-item-link active" href="${pageContext.request.contextPath}/dashboard">
            <i class="bi bi-house-door-fill"></i>
            <spring:message code="dashboard.menu.home"/>
        </a>
        <a class="nav-item-link" href="#">
            <i class="bi bi-star"></i>
            <spring:message code="dashboard.menu.starred"/>
        </a>
        <a class="nav-item-link" href="#">
            <i class="bi bi-clock-history"></i>
            <spring:message code="dashboard.menu.recent"/>
        </a>
        <a class="nav-item-link" href="#">
            <i class="bi bi-trash3"></i>
            <spring:message code="dashboard.menu.trash"/>
        </a>
    </nav>

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
</aside>

<%-- ══ MAIN ══ --%>
<main class="main">

    <%-- Breadcrumb --%>
    <div class="breadcrumb-area">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item">
                    <a href="${pageContext.request.contextPath}/dashboard">
                        <spring:message code="dashboard.breadcrumb.myFiles"/>
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
                <a class="folder-card"
                   href="${pageContext.request.contextPath}/dashboard?folderId=${folder.id}">
                    <i class="bi bi-folder-fill folder-icon"></i>
                    <span class="folder-name">${folder.name}</span>
                    <button class="card-menu-btn"
                            data-bs-toggle="dropdown"
                            aria-expanded="false"
                            onclick="event.preventDefault(); event.stopPropagation();">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-1">
                        <li>
                            <a class="dropdown-item small py-2" href="#">
                                <i class="bi bi-pencil me-2"></i>Yeniden Adlandır
                            </a>
                        </li>
                        <li>
                            <a class="dropdown-item small py-2 text-danger" href="#">
                                <i class="bi bi-trash3 me-2"></i><spring:message code="common.delete"/>
                            </a>
                        </li>
                    </ul>
                </a>
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
                    <button class="card-menu-btn"
                            data-bs-toggle="dropdown"
                            aria-expanded="false"
                            onclick="event.stopPropagation();">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-1">
                        <li>
                            <a class="dropdown-item small py-2"
                               href="${pageContext.request.contextPath}/file/download?fileId=${file.id}">
                                <i class="bi bi-download me-2"></i><spring:message code="common.download"/>
                            </a>
                        </li>
                        <li><hr class="dropdown-divider my-1"></li>
                        <li>
                            <form action="${pageContext.request.contextPath}/file/delete" method="post" class="m-0">
                                <input type="hidden" name="fileId" value="${file.id}">
                                <input type="hidden" name="folderId" value="${currentFolder.id}">
                                <button type="submit" class="dropdown-item small py-2 text-danger">
                                    <i class="bi bi-trash3 me-2"></i><spring:message code="common.delete"/>
                                </button>
                            </form>
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

<%-- ══ NEW MENU MODAL (Google Drive tarzı seçenek listesi) ══ --%>
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function switchModal(hideId, showId) {
        bootstrap.Modal.getInstance(document.getElementById(hideId)).hide();
        document.getElementById(hideId).addEventListener('hidden.bs.modal', function onHidden() {
            this.removeEventListener('hidden.bs.modal', onHidden);
            bootstrap.Modal.getOrCreateInstance(document.getElementById(showId)).show();
        });
    }

    // Klasör seçildiğinde relativePaths hidden input'larını hazırla
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
        document.getElementById('folderFileCount').textContent =
            count + ' dosya seçildi';
    });
</script>
</body>
</html>
