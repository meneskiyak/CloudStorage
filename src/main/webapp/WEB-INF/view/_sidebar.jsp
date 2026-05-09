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
    <div class="search-wrap d-none d-md-block" style="position:relative">
        <input type="search" id="globalSearchInput" class="search-input"
               placeholder="<spring:message code="dashboard.search.placeholder"/>"
               autocomplete="off">
        <div id="searchDropdown" style="
            display:none;
            position:absolute;
            top:calc(100% + 6px);
            left:0; right:0;
            background:#fff;
            border-radius:12px;
            box-shadow:0 4px 20px rgba(0,0,0,.15);
            z-index:2000;
            max-height:400px;
            overflow-y:auto;
        "></div>
    </div>

    <div class="topbar-user">
        <div class="dropdown user-dropdown">
            <button class="avatar-btn" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                <c:set var="names" value="${fn:split(user.fullName, ' ')}"/>
                <c:out value="${fn:substring(names[0], 0, 1)}${fn:length(names) > 1 ? fn:substring(names[fn:length(names)-1], 0, 1) : ''}"/>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0">
                <li class="px-3 py-2 border-bottom mb-1">
                    <div class="fw-bold small text-truncate" style="max-width: 150px;">${user.fullName}</div>
                    <div class="text-secondary text-truncate" style="font-size: 0.75rem; max-width: 150px;">${user.email}</div>
                </li>
                <li>
                    <a class="dropdown-item" href="#">
                        <i class="bi bi-gear"></i><spring:message code="common.settings"/>
                    </a>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                    <a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/logout">
                        <i class="bi bi-box-arrow-right"></i><spring:message code="dashboard.logout"/>
                    </a>
                </li>
            </ul>
        </div>
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
            <c:set var="limit" value="${user.uploadLimitBytes > 0 ? user.uploadLimitBytes : 1}"/>
            <c:set var="pct" value="${(user.usedBytes / limit) * 100}"/>
            <div class="progress mb-2" style="height: 4px; background-color: #e8eaed;">
                <div class="progress-bar" style="width:${pct}%"></div>
            </div>
            <div class="storage-label">
                <fmt:formatNumber var="usedGb" value="${user.usedBytes / 1073741824.0}" maxFractionDigits="2"/>
                <fmt:formatNumber var="totalGb" value="${user.uploadLimitBytes / 1073741824.0}" maxFractionDigits="1"/>
                ${usedGb} GB / ${totalGb} GB
            </div>
        </div>
    </c:if>
</aside>

<%-- ══ NEW MENU MODAL ══ --%>
<div class="modal fade new-menu-modal" id="newMenuModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <button class="new-menu-item" onclick="closeNewMenu(); switchModal('newMenuModal','newFolderModal')">
                <i class="bi bi-folder-plus"></i>
                <spring:message code="dashboard.new.folder"/>
            </button>
            <div class="new-menu-divider"></div>
            <button class="new-menu-item" onclick="closeNewMenu(); document.getElementById('fileInput').click()">
                <i class="bi bi-file-earmark-arrow-up"></i>
                <spring:message code="dashboard.new.fileUpload"/>
            </button>
            <button class="new-menu-item" onclick="closeNewMenu(); document.getElementById('folderInput').click()">
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

<%-- ══ GİZLİ UPLOAD FORMLARI ══ --%>
<form id="uploadFileForm" action="${pageContext.request.contextPath}/file/upload"
      method="post" enctype="multipart/form-data" style="display:none">
    <input type="hidden" name="folderId" value="${currentFolder.id}">
    <input type="file" id="fileInput" name="file">
</form>

<form id="uploadFolderForm" action="${pageContext.request.contextPath}/folder/upload"
      method="post" enctype="multipart/form-data" style="display:none">
    <input type="hidden" name="parentId" value="${currentFolder.id}">
    <div id="folderRelativePaths"></div>
    <input type="file" id="folderInput" name="files" webkitdirectory directory multiple>
</form>

<script>
    /* ── Search dropdown ── */
    (function () {
        var input = document.getElementById('globalSearchInput');
        var dropdown = document.getElementById('searchDropdown');
        var debounceTimer = null;
        var ctx = '${pageContext.request.contextPath}';

        function getMimeIcon(mime) {
            if (!mime) return 'bi-file-earmark';
            if (mime.startsWith('image/'))   return 'bi-file-image';
            if (mime === 'application/pdf')  return 'bi-file-pdf';
            if (mime.startsWith('video/'))   return 'bi-file-play';
            if (mime.startsWith('audio/'))   return 'bi-file-music';
            if (mime.includes('zip') || mime.includes('compressed')) return 'bi-file-zip';
            if (mime.includes('word'))       return 'bi-file-word';
            if (mime.includes('sheet') || mime.includes('excel')) return 'bi-file-excel';
            return 'bi-file-earmark';
        }

        function closeDropdown() {
            dropdown.style.display = 'none';
            dropdown.innerHTML = '';
        }

        function renderDropdown(data, query) {
            dropdown.innerHTML = '';
            var folders = data.folders || [];
            var files   = data.files   || [];

            if (folders.length === 0 && files.length === 0) {
                dropdown.innerHTML = '<div style="padding:16px;text-align:center;color:#5f6368;font-size:.9rem">Sonuç bulunamadı</div>';
                dropdown.style.display = 'block';
                return;
            }

            function makeItem(icon, name, href) {
                var a = document.createElement('a');
                a.href = href;
                a.style.cssText = 'display:flex;align-items:center;gap:12px;padding:10px 16px;text-decoration:none;color:#3c4043;font-size:.9rem;transition:background .12s';
                a.onmouseenter = function(){ this.style.background='#f1f3f4'; };
                a.onmouseleave = function(){ this.style.background=''; };
                a.innerHTML = '<i class="bi ' + icon + '" style="font-size:1.1rem;color:#5f6368;flex-shrink:0"></i>'
                            + '<span style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + name + '</span>';
                return a;
            }

            if (folders.length > 0) {
                var lbl = document.createElement('div');
                lbl.style.cssText = 'padding:8px 16px 4px;font-size:.75rem;font-weight:600;color:#5f6368;text-transform:uppercase;letter-spacing:.5px';
                lbl.textContent = 'Klasörler';
                dropdown.appendChild(lbl);
                folders.forEach(function(f) {
                    dropdown.appendChild(makeItem('bi-folder-fill', f.name, ctx + '/dashboard?folderId=' + f.id));
                });
            }

            if (files.length > 0) {
                if (folders.length > 0) {
                    var sep = document.createElement('div');
                    sep.style.cssText = 'border-top:1px solid #e0e0e0;margin:4px 0';
                    dropdown.appendChild(sep);
                }
                var lbl2 = document.createElement('div');
                lbl2.style.cssText = 'padding:8px 16px 4px;font-size:.75rem;font-weight:600;color:#5f6368;text-transform:uppercase;letter-spacing:.5px';
                lbl2.textContent = 'Dosyalar';
                dropdown.appendChild(lbl2);
                files.forEach(function(f) {
                    var href = f.folderId ? ctx + '/dashboard?folderId=' + f.folderId : ctx + '/dashboard';
                    dropdown.appendChild(makeItem(getMimeIcon(f.mimeType), f.name, href));
                });
            }

            dropdown.style.display = 'block';
        }

        input.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            var q = this.value.trim();
            if (q.length === 0) { closeDropdown(); return; }

             debounceTimer = setTimeout(function () {
                fetch(ctx + '/search?q=' + encodeURIComponent(q), {
                    headers: { 'Accept': 'application/json' }
                })
                .then(function(r){ return r.json(); })
                .then(function(data){ renderDropdown(data, q); })
                .catch(function(){ closeDropdown(); });
            }, 300);
        });

        input.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') { clearTimeout(debounceTimer); closeDropdown(); this.blur(); }
        });

        document.addEventListener('click', function(e) {
            if (!input.contains(e.target) && !dropdown.contains(e.target)) closeDropdown();
        });
    })();

    function closeNewMenu() {
        var el = document.getElementById('newMenuModal');
        var m = bootstrap.Modal.getInstance(el);
        if (m) m.hide();
    }

    function switchModal(hideId, showId) {
        var hideEl = document.getElementById(hideId);
        var m = bootstrap.Modal.getInstance(hideEl);
        if (m) {
            m.hide();
            hideEl.addEventListener('hidden.bs.modal', function onHidden() {
                this.removeEventListener('hidden.bs.modal', onHidden);
                bootstrap.Modal.getOrCreateInstance(document.getElementById(showId)).show();
            });
        } else {
            bootstrap.Modal.getOrCreateInstance(document.getElementById(showId)).show();
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.getElementById('fileInput').addEventListener('change', function () {
            if (this.files.length > 0) {
                document.getElementById('uploadFileForm').submit();
            }
        });

        document.getElementById('folderInput').addEventListener('change', function () {
            if (this.files.length === 0) return;
            var container = document.getElementById('folderRelativePaths');
            container.innerHTML = '';
            for (var i = 0; i < this.files.length; i++) {
                var input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'relativePaths';
                input.value = this.files[i].webkitRelativePath;
                container.appendChild(input);
            }
            document.getElementById('uploadFolderForm').submit();
        });
    });
</script>
