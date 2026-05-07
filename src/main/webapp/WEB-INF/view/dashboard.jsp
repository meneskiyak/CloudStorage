<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="app.name"/> | Dashboard</title>
    
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    
    <style>
        :root {
            --sidebar-width: 280px;
            --google-blue: #1a73e8;
            --google-bg: #f8f9fa;
        }
        
        body {
            background-color: var(--google-bg);
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            overflow-x: hidden;
        }
        
        /* Navbar Styling */
        .navbar {
            background-color: white;
            border-bottom: 1px solid #dee2e6;
            height: 64px;
            z-index: 1030;
        }
        
        .search-container {
            max-width: 720px;
            width: 100%;
        }
        
        .search-bar {
            background-color: #f1f3f4;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            width: 100%;
        }
        
        .search-bar:focus {
            background-color: white;
            box-shadow: 0 1px 1px 0 rgba(65,69,73,0.3), 0 1px 3px 1px rgba(65,69,73,0.15);
            outline: none;
        }
        
        /* Sidebar Styling */
        .sidebar {
            width: var(--sidebar-width);
            height: calc(100vh - 64px);
            position: fixed;
            left: 0;
            top: 64px;
            padding: 16px;
            background-color: var(--google-bg);
            transition: transform 0.3s ease;
            z-index: 1020;
        }
        
        @media (max-width: 991.98px) {
            .sidebar {
                transform: translateX(-100%);
            }
            .sidebar.show {
                transform: translateX(0);
                box-shadow: 0 0 15px rgba(0,0,0,0.1);
                background-color: white;
            }
            .main-content {
                margin-left: 0 !important;
            }
        }
        
        .btn-new {
            padding: 12px 24px;
            border-radius: 16px;
            box-shadow: 0 1px 2px 0 rgba(60,64,67,0.302), 0 1px 3px 1px rgba(60,64,67,0.149);
            background-color: white;
            border: none;
            font-weight: 500;
            margin-bottom: 24px;
            transition: box-shadow 0.2s;
        }
        
        .btn-new:hover {
            box-shadow: 0 4px 4px 0 rgba(60,64,67,0.302), 0 1px 3px 1px rgba(60,64,67,0.149);
            background-color: #f1f3f4;
        }
        
        .nav-link {
            border-radius: 0 24px 24px 0;
            padding: 10px 24px;
            color: #3c4043;
            font-weight: 500;
            margin-left: -16px;
            margin-right: -16px;
        }
        
        .nav-link.active {
            background-color: #e8f0fe !important;
            color: var(--google-blue) !important;
        }
        
        .nav-link:hover:not(.active) {
            background-color: #f1f3f4;
        }
        
        .nav-link i {
            margin-right: 12px;
            font-size: 1.2rem;
        }
        
        /* Main Content Styling */
        .main-content {
            margin-left: var(--sidebar-width);
            padding: 24px;
            min-height: calc(100vh - 64px);
            transition: margin-left 0.3s ease;
        }
        
        .view-toggle-btn {
            color: #5f6368;
            border: none;
            background: none;
            padding: 8px;
            border-radius: 50%;
        }
        
        .view-toggle-btn:hover {
            background-color: #f1f3f4;
        }
        
        .view-toggle-btn.active {
            color: var(--google-blue);
        }
        
        /* Card View Styling */
        .file-card {
            background-color: white;
            border: 1px solid #dadce0;
            border-radius: 8px;
            padding: 16px;
            transition: all 0.2s;
            cursor: pointer;
            height: 100%;
            position: relative;
        }
        
        .file-card:hover {
            background-color: #f1f3f4;
            box-shadow: 0 1px 2px 0 rgba(60,64,67,0.3), 0 1px 3px 1px rgba(60,64,67,0.15);
        }
        
        .file-icon {
            font-size: 2.5rem;
            color: #5f6368;
            margin-bottom: 12px;
        }
        
        .folder-icon {
            color: #5f6368;
        }
        
        /* Progress Bar Styling */
        .storage-info {
            margin-top: auto;
            padding: 16px;
            font-size: 0.875rem;
        }
        
        .progress {
            height: 4px;
            margin-bottom: 8px;
        }
        
        /* Hidden classes for toggling views */
        .d-none-view {
            display: none !important;
        }

        .breadcrumb-item + .breadcrumb-item::before {
            content: "\F285";
            font-family: "bootstrap-icons";
        }

        .card-actions {
            position: absolute;
            top: 8px;
            right: 8px;
            opacity: 0;
            transition: opacity 0.2s;
        }

        .file-card:hover .card-actions {
            opacity: 1;
        }
    </style>
</head>
<body>

    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg fixed-top px-3">
        <div class="container-fluid">
            <button class="btn btn-link d-lg-none me-2 text-dark" id="sidebarToggle">
                <i class="bi bi-list fs-3"></i>
            </button>
            
            <a class="navbar-brand d-flex align-items-center" href="${pageContext.request.contextPath}/dashboard">
                <i class="bi bi-cloud-fill text-primary me-2 fs-3"></i>
                <span class="fw-semibold"><spring:message code="app.name"/></span>
            </a>
            
            <div class="search-container mx-auto d-none d-md-block">
                <div class="position-relative">
                    <i class="bi bi-search position-absolute top-50 start-0 translate-middle-y ms-3 text-secondary"></i>
                    <spring:message code="dashboard.search.placeholder" var="searchPlaceholder"/>
                    <input type="text" class="search-bar ps-5" placeholder="${searchPlaceholder}">
                </div>
            </div>
            
            <div class="d-flex align-items-center">
                <div class="dropdown">
                    <button class="btn btn-link text-dark dropdown-toggle text-decoration-none d-flex align-items-center" type="button" id="userDropdown" data-bs-toggle="dropdown">
                        <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-2" style="width: 32px; height: 32px;">
                            ${user.fullName.substring(0,1).toUpperCase()}
                        </div>
                        <span class="d-none d-sm-inline">${user.fullName}</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0">
                        <li><a class="dropdown-item py-2" href="#"><i class="bi bi-person me-2"></i> <spring:message code="dashboard.profile"/></a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item py-2 text-danger" href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right me-2"></i> <spring:message code="dashboard.logout"/></a></li>
                    </ul>
                </div>
            </div>
        </div>
    </nav>

    <!-- Sidebar -->
    <div class="sidebar d-flex flex-column" id="sidebar">
        <div class="dropdown mb-4">
            <button class="btn btn-new d-flex align-items-center" data-bs-toggle="dropdown">
                <i class="bi bi-plus-lg fs-4 me-2"></i>
                <span><spring:message code="dashboard.new"/></span>
            </button>
            <ul class="dropdown-menu shadow border-0 p-2">
                <li>
                    <a class="dropdown-item rounded py-2" href="#" data-bs-toggle="modal" data-bs-target="#newFolderModal">
                        <i class="bi bi-folder-plus me-2 text-secondary"></i> <spring:message code="dashboard.new.folder"/>
                    </a>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                    <a class="dropdown-item rounded py-2" href="#" data-bs-toggle="modal" data-bs-target="#uploadFileModal">
                        <i class="bi bi-file-earmark-arrow-up me-2 text-secondary"></i> <spring:message code="dashboard.new.fileUpload"/>
                    </a>
                </li>
            </ul>
        </div>
        
        <nav class="nav flex-column mb-4">
            <a class="nav-link active" href="${pageContext.request.contextPath}/dashboard"><i class="bi bi-house-door"></i> <spring:message code="dashboard.menu.home"/></a>
            <a class="nav-link" href="#"><i class="bi bi-star"></i> <spring:message code="dashboard.menu.starred"/></a>
            <a class="nav-link" href="#"><i class="bi bi-clock-history"></i> <spring:message code="dashboard.menu.recent"/></a>
            <a class="nav-link" href="#"><i class="bi bi-trash"></i> <spring:message code="dashboard.menu.trash"/></a>
        </nav>
        
        <div class="storage-info mt-auto">
            <div class="d-flex align-items-center mb-2">
                <i class="bi bi-cloud me-2"></i>
                <span><spring:message code="dashboard.storage.title"/></span>
            </div>
            <div class="progress">
                <c:set var="percent" value="${(user.usedBytes / user.uploadLimitBytes) * 100}"/>
                <div class="progress-bar" role="progressbar" style="width: ${percent}%" aria-valuenow="${percent}" aria-valuemin="0" aria-valuemax="100"></div>
            </div>
            <div class="text-secondary small">
                <fmt:formatNumber var="usedMb" value="${user.usedBytes / 1024 / 1024}" maxFractionDigits="2"/>
                <fmt:formatNumber var="totalMb" value="${user.uploadLimitBytes / 1024 / 1024}" maxFractionDigits="2"/>
                <spring:message code="dashboard.storage.using" arguments="${usedMb},${totalMb}"/>
            </div>
        </div>
    </div>

    <!-- Main Content -->
    <main class="main-content mt-5 pt-4">
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item">
                        <a href="${pageContext.request.contextPath}/dashboard" class="text-decoration-none text-dark fw-medium">
                            <spring:message code="dashboard.breadcrumb.myFiles"/>
                        </a>
                    </li>
                    <c:if test="${not empty currentFolder}">
                        <li class="breadcrumb-item active" aria-current="page">${currentFolder.name}</li>
                    </c:if>
                </ol>
            </nav>
            
            <div class="d-flex gap-2">
                <spring:message code="dashboard.view.grid" var="gridViewTitle"/>
                <button class="view-toggle-btn active" id="gridToggleBtn" title="${gridViewTitle}">
                    <i class="bi bi-grid-3x3-gap"></i>
                </button>
                <spring:message code="dashboard.view.list" var="listViewTitle"/>
                <button class="view-toggle-btn" id="listToggleBtn" title="${listViewTitle}">
                    <i class="bi bi-list-task"></i>
                </button>
            </div>
        </div>

        <!-- Content Area -->
        <div id="contentArea">
            <!-- Grid View -->
            <div id="gridView" class="row g-3">
                <!-- Folders -->
                <c:forEach items="${folders}" var="f">
                    <div class="col-6 col-sm-4 col-md-4 col-lg-3 col-xl-2">
                        <div class="file-card d-flex flex-column align-items-center" onclick="location.href='${pageContext.request.contextPath}/dashboard?folderId=${f.id}'">
                            <i class="bi bi-folder-fill fs-1 folder-icon mb-2 text-warning"></i>
                            <span class="text-truncate w-100 text-center small" title="${f.name}">${f.name}</span>
                            <div class="card-actions">
                                <div class="dropdown" onclick="event.stopPropagation()">
                                    <button class="btn btn-sm btn-link text-dark p-0" data-bs-toggle="dropdown">
                                        <i class="bi bi-three-dots-vertical"></i>
                                    </button>
                                    <ul class="dropdown-menu shadow border-0">
                                        <li><a class="dropdown-item py-2 text-danger" href="#"><i class="bi bi-trash me-2"></i> <spring:message code="common.delete"/></a></li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
                
                <!-- Files -->
                <c:forEach items="${files}" var="file">
                    <div class="col-6 col-sm-4 col-md-4 col-lg-3 col-xl-2">
                        <div class="file-card d-flex flex-column align-items-center">
                            <c:choose>
                                <c:when test="${file.mimeType.contains('pdf')}">
                                    <i class="bi bi-file-earmark-pdf-fill fs-1 text-danger mb-2"></i>
                                </c:when>
                                <c:when test="${file.mimeType.contains('image')}">
                                    <i class="bi bi-file-earmark-image-fill fs-1 text-success mb-2"></i>
                                </c:when>
                                <c:otherwise>
                                    <i class="bi bi-file-earmark-fill fs-1 text-secondary mb-2"></i>
                                </c:otherwise>
                            </c:choose>
                            <span class="text-truncate w-100 text-center small" title="${file.originalName}">${file.originalName}</span>
                            <div class="card-actions">
                                <div class="dropdown">
                                    <button class="btn btn-sm btn-link text-dark p-0" data-bs-toggle="dropdown">
                                        <i class="bi bi-three-dots-vertical"></i>
                                    </button>
                                    <ul class="dropdown-menu shadow border-0">
                                        <li><a class="dropdown-item py-2" href="#"><i class="bi bi-download me-2"></i> İndir</a></li>
                                        <li><hr class="dropdown-divider"></li>
                                        <li>
                                            <form action="${pageContext.request.contextPath}/file/delete" method="post">
                                                <input type="hidden" name="fileId" value="${file.id}">
                                                <button type="submit" class="dropdown-item py-2 text-danger">
                                                    <i class="bi bi-trash me-2"></i> <spring:message code="common.delete"/>
                                                </button>
                                            </form>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty folders && empty files}">
                    <div class="col-12 text-center py-5">
                        <i class="bi bi-cloud-slash fs-1 text-secondary opacity-25"></i>
                        <p class="text-secondary mt-3">Bu klasör boş.</p>
                    </div>
                </c:if>
            </div>

            <!-- List View (Hidden by default) -->
            <div id="listView" class="d-none-view">
                <div class="table-responsive bg-white rounded shadow-sm">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="bg-light">
                            <tr>
                                <th class="ps-4"><spring:message code="dashboard.table.name"/></th>
                                <th><spring:message code="dashboard.table.owner"/></th>
                                <th><spring:message code="dashboard.table.lastModified"/></th>
                                <th><spring:message code="dashboard.table.size"/></th>
                                <th class="pe-4"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${folders}" var="f">
                                <tr onclick="location.href='${pageContext.request.contextPath}/dashboard?folderId=${f.id}'" style="cursor: pointer;">
                                    <td class="ps-4">
                                        <i class="bi bi-folder-fill text-warning me-2"></i>
                                        ${f.name}
                                    </td>
                                    <td>${f.owner.fullName}</td>
                                    <td><fmt:formatDate value="${f.updatedAt}" pattern="dd MMM yyyy"/></td>
                                    <td>--</td>
                                    <td class="pe-4 text-end">
                                        <button class="btn btn-link text-dark"><i class="bi bi-three-dots-vertical"></i></button>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:forEach items="${files}" var="file">
                                <tr>
                                    <td class="ps-4">
                                        <c:choose>
                                            <c:when test="${file.mimeType.contains('pdf')}">
                                                <i class="bi bi-file-earmark-pdf-fill text-danger me-2"></i>
                                            </c:when>
                                            <c:when test="${file.mimeType.contains('image')}">
                                                <i class="bi bi-file-earmark-image-fill text-success me-2"></i>
                                            </c:when>
                                            <c:otherwise>
                                                <i class="bi bi-file-earmark-fill text-secondary me-2"></i>
                                            </c:otherwise>
                                        </c:choose>
                                        ${file.originalName}
                                    </td>
                                    <td>${file.owner.fullName}</td>
                                    <td><fmt:formatDate value="${file.updatedAt}" pattern="dd MMM yyyy"/></td>
                                    <td>
                                        <fmt:formatNumber value="${file.fileSizeBytes / 1024 / 1024}" maxFractionDigits="2"/> MB
                                    </td>
                                    <td class="pe-4 text-end">
                                        <button class="btn btn-link text-dark"><i class="bi bi-three-dots-vertical"></i></button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>

    <!-- Modals -->
    <!-- New Folder Modal -->
    <div class="modal fade" id="newFolderModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow">
                <form action="${pageContext.request.contextPath}/folder/create" method="post">
                    <input type="hidden" name="parentId" value="${currentFolder.id}">
                    <div class="modal-header">
                        <h5 class="modal-title"><spring:message code="dashboard.modal.newFolder.title"/></h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label"><spring:message code="dashboard.modal.newFolder.name"/></label>
                            <input type="text" class="form-control" name="name" required autofocus>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal"><spring:message code="common.cancel"/></button>
                        <button type="submit" class="btn btn-primary px-4"><spring:message code="dashboard.modal.newFolder.create"/></button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Upload File Modal -->
    <div class="modal fade" id="uploadFileModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow">
                <form action="${pageContext.request.contextPath}/file/upload" method="post" id="uploadForm">
                    <input type="hidden" name="folderId" value="${currentFolder.id}">
                    <!-- Controller mock olduğu için bunları hidden olarak gönderiyoruz -->
                    <input type="hidden" name="fileName" id="mockFileName">
                    <input type="hidden" name="fileSize" id="mockFileSize">
                    <input type="hidden" name="mimeType" id="mockMimeType">
                    
                    <div class="modal-header">
                        <h5 class="modal-title"><spring:message code="dashboard.modal.uploadFile.title"/></h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label"><spring:message code="dashboard.modal.uploadFile.select"/></label>
                            <input type="file" class="form-control" id="fileInput" required>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal"><spring:message code="common.cancel"/></button>
                        <button type="submit" class="btn btn-primary px-4"><spring:message code="dashboard.modal.uploadFile.upload"/></button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Scripts -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const sidebar = document.getElementById('sidebar');
        const sidebarToggle = document.getElementById('sidebarToggle');
        const gridView = document.getElementById('gridView');
        const listView = document.getElementById('listView');
        const gridToggleBtn = document.getElementById('gridToggleBtn');
        const listToggleBtn = document.getElementById('listToggleBtn');
        const fileInput = document.getElementById('fileInput');
        const uploadForm = document.getElementById('uploadForm');

        // Sidebar Toggle for Mobile
        sidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });

        // Close sidebar when clicking outside on mobile
        document.addEventListener('click', (e) => {
            if (window.innerWidth < 992 && !sidebar.contains(e.target) && !sidebarToggle.contains(e.target)) {
                sidebar.classList.remove('show');
            }
        });

        // View Toggling
        gridToggleBtn.addEventListener('click', () => {
            gridView.classList.remove('d-none-view');
            listView.classList.add('d-none-view');
            gridToggleBtn.classList.add('active');
            listToggleBtn.classList.remove('active');
        });

        listToggleBtn.addEventListener('click', () => {
            listView.classList.remove('d-none-view');
            gridView.classList.add('d-none-view');
            listToggleBtn.classList.add('active');
            gridToggleBtn.classList.remove('active');
        });

        // Mock File Upload Data
        fileInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                document.getElementById('mockFileName').value = file.name;
                document.getElementById('mockFileSize').value = file.size;
                document.getElementById('mockMimeType').value = file.type || 'application/octet-stream';
            }
        });
    </script>
</body>
</html>
