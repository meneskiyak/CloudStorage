<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
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
        margin-left: auto;
        display: flex;
        align-items: center;
        padding-right: 8px;
    }

    .avatar-btn {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: #1a73e8;
        color: #fff;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.9rem;
        cursor: pointer;
        border: 2px solid transparent;
        transition: border-color 0.2s;
        padding: 0;
    }

    .avatar-btn:hover { border-color: #d2e3fc; }

    .user-dropdown .dropdown-menu {
        margin-top: 8px;
        border: none;
        box-shadow: 0 2px 10px rgba(0,0,0,.1);
        border-radius: 8px;
        padding: 8px 0;
    }

    .user-dropdown .dropdown-item {
        padding: 8px 16px;
        font-size: 0.9rem;
        color: #3c4043;
    }

    .user-dropdown .dropdown-item i {
        font-size: 1.1rem;
        color: #5f6368;
        width: 20px;
        margin-right: 10px;
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
        background: var(--surface);
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

    .btn-new-placeholder {
        height: 56px;
        margin-bottom: 8px;
    }

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

    /* ── Folder / File grid ── */
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
    }

    .folder-card:hover { background: #f1f3f4; color: #3c4043; box-shadow: 0 1px 4px rgba(0,0,0,.12); }
    .folder-card i.folder-icon { font-size: 1.4rem; color: #5f6368; flex-shrink: 0; }
    .folder-card .folder-name { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

    .folder-card .card-menu-btn {
        opacity: 1;
        background: none;
        border: none;
        padding: 4px 6px;
        border-radius: 50%;
        color: #5f6368;
        font-size: 1rem;
        transition: background 0.15s;
        flex-shrink: 0;
    }

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

    /* ── Modal animasyon hızı ── */
    .modal.fade .modal-dialog { transition: transform 0.15s ease-out; }
    .modal.fade { transition: opacity 0.15s linear; }

    /* ── Dosya thumbnail kartı ── */
    .folder-card.has-thumb {
        flex-direction: column;
        align-items: stretch;
        padding: 0;
    }
    .folder-card.has-thumb .card-meta-row {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 10px;
    }
    .file-thumb-wrap {
        width: 100%;
        height: 120px;
        overflow: hidden;
        border-radius: 8px 8px 0 0;
        background: #f1f3f4;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
    }
    .file-thumb-wrap img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }
    .file-thumb-wrap .thumb-icon {
        font-size: 2.5rem;
        color: #bdc1c6;
    }

    /* ── Preview modal ── */
    #previewModal .modal-dialog { max-width: 860px; }
    #previewModal .modal-body {
        padding: 0;
        background: #202124;
        min-height: 400px;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    #previewModal .preview-img {
        max-width: 100%;
        max-height: 75vh;
        object-fit: contain;
        display: block;
        margin: 0 auto;
    }
    #previewModal .preview-pdf-frame {
        width: 100%;
        height: 75vh;
        border: none;
        display: block;
    }

    /* ── Drag & Drop ── */
    .folder-card.drag-over {
        border: 2px dashed var(--accent) !important;
        background: #e8f0fe !important;
        transform: scale(1.02);
    }
</style>
