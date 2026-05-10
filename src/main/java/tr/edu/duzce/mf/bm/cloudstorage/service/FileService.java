package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.AccessDeniedException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FileNotFoundException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.StorageQuotaExceededException;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FileItemDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FolderDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = Exception.class)
public class FileService {

    @Autowired
    private FileItemDao fileItemDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private MinioService minioService;

    @Autowired
    private FolderDao folderDao;

    public List<FileItem> getUserFiles(Folder folder, User owner) {
        return fileItemDao.findFilesByFolderAndOwner(folder, owner);
    }

    public List<FileItem> searchUserFiles(User owner, String keyword, String mimeType) {
        return fileItemDao.searchFiles(owner, keyword, mimeType);
    }

    @Transactional(readOnly = false)
    public void uploadFile(MultipartFile multipartFile, Long folderId, User currentUser) {
        if (currentUser.isLimitExceeded(multipartFile.getSize()))
            throw new StorageQuotaExceededException("Kota aşıldı!");

        Folder folder = null;
        if (folderId != null) {
            folder = folderDao.findById(folderId);
            if (folder == null)
                throw new tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderNotFoundException("Hedef klasör bulunamadı!");
            
            // Güvenlik ve İş Mantığı Kontrolleri
            if (!folder.getOwner().getId().equals(currentUser.getId()))
                throw new AccessDeniedException("Bu klasöre dosya yükleme yetkiniz yok!");
            
            if (folder.isDeleted())
                throw new tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderNotFoundException("Silinmiş bir klasöre dosya yüklenemez!");
        }

        String storedName;
        try {
            storedName = minioService.uploadFile(multipartFile);
        } catch (Exception e) {
            throw new tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.StorageException("Dosya MinIO'ya yüklenemedi!", e);
        }

        FileItem fileItem = new FileItem();
        fileItem.setOriginalName(multipartFile.getOriginalFilename());
        fileItem.setStoredName(storedName);
        fileItem.setStoragePath(minioService.bucket);
        fileItem.setFileSizeBytes(multipartFile.getSize());
        fileItem.setMimeType(multipartFile.getContentType() != null
                ? multipartFile.getContentType() : "application/octet-stream");
        fileItem.setFolder(folder);
        fileItem.setOwner(currentUser);

        currentUser.setUsedBytes(currentUser.getUsedBytes() + multipartFile.getSize());
        userDao.update(currentUser);
        fileItemDao.save(fileItem);
    }

    @Transactional(readOnly = false)
    public void moveFile(Long fileId, Folder targetFolder, User currentUser) {
        FileItem file = fileItemDao.findById(fileId);
        if (file == null)
            throw new FileNotFoundException("Dosya bulunamadı");
        
        if (!file.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu dosyaya erişim yetkiniz yok");

        // Hedef klasör kontrolleri
        if (targetFolder != null) {
            if (!targetFolder.getOwner().getId().equals(currentUser.getId()))
                throw new AccessDeniedException("Hedef klasöre erişim yetkiniz yok!");
            
            if (targetFolder.isDeleted())
                throw new tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderNotFoundException("Silinmiş bir klasöre dosya taşınamaz!");
        }

        file.setFolder(targetFolder);
    }

    @Transactional(readOnly = false)
    public void copyFile(Long fileId, Folder targetFolder, User currentUser) {
        FileItem original = fileItemDao.findById(fileId);
        if (original == null)
            throw new FileNotFoundException("Dosya bulunamadı");
        
        if (!original.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu dosyaya erişim yetkiniz yok");

        // Hedef klasör kontrolleri
        if (targetFolder != null) {
            if (!targetFolder.getOwner().getId().equals(currentUser.getId()))
                throw new AccessDeniedException("Hedef klasöre erişim yetkiniz yok!");
            
            if (targetFolder.isDeleted())
                throw new tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderNotFoundException("Silinmiş bir klasöre dosya kopyalanamaz!");
        }

        if (currentUser.isLimitExceeded(original.getFileSizeBytes()))
            throw new StorageQuotaExceededException("Kopya için yeterli alan yok");

        String destName = UUID.randomUUID() + "_" + original.getOriginalName();
        try {
            minioService.copyFile(original.getStoredName(), destName);
        } catch (Exception e) {
            throw new tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.StorageException("Dosya MinIO'da kopyalanamadı!", e);
        }

        FileItem copy = new FileItem();
        copy.setOriginalName("Kopya_" + original.getOriginalName());
        copy.setStoredName(destName);
        copy.setStoragePath(original.getStoragePath());
        copy.setFileSizeBytes(original.getFileSizeBytes());
        copy.setMimeType(original.getMimeType());
        copy.setFolder(targetFolder);
        copy.setOwner(currentUser);

        currentUser.setUsedBytes(currentUser.getUsedBytes() + copy.getFileSizeBytes());
        userDao.update(currentUser);
        fileItemDao.save(copy);
    }

    @Transactional(readOnly = false)
    public void softDeleteFile(Long fileId, User currentUser) {
        FileItem file = fileItemDao.findById(fileId);
        if (file == null)
            throw new FileNotFoundException("Dosya bulunamadı");
        if (!file.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu dosyaya erişim yetkiniz yok");

        // Çöp kutusundaki dosyalar kota kaplamaya devam etsin diye buradan çıkarma işlemini kaldırdık.
        fileItemDao.softDelete(file);
    }

    @Transactional(readOnly = false)
    public void renameFile(Long fileId, String newName, User currentUser) {
        FileItem file = fileItemDao.findById(fileId);
        if (file == null)
            throw new FileNotFoundException("Dosya bulunamadı");
        if (!file.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu dosyaya erişim yetkiniz yok");

        file.setOriginalName(newName);
    }

    public List<FileItem> getDeletedFiles(User owner) {
        return fileItemDao.findDeletedByOwner(owner);
    }

    @Transactional(readOnly = false)
    public void restoreFile(Long fileId, User currentUser) {
        FileItem file = fileItemDao.findById(fileId);
        if (file == null)
            throw new FileNotFoundException("Dosya bulunamadı");
        if (!file.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu dosyaya erişim yetkiniz yok");

        // Kota zaten düşülmediği için geri yüklemede kota kontrolüne gerek yok.
        file.setDeleted(false);
    }

    @Transactional(readOnly = false)
    public void permanentlyDeleteFile(Long fileId, User currentUser) {
        FileItem file = fileItemDao.findById(fileId);
        if (file == null)
            throw new FileNotFoundException("Dosya bulunamadı");
        if (!file.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu dosyaya erişim yetkiniz yok");

        // MinIO'dan sil
        try {
            minioService.deleteFile(file.getStoredName());
        } catch (Exception e) {
            // Loglanabilir
        }

        // Kullanıcıyı DB'den tekrar yükleyerek güncel kotayı alalım
        User user = userDao.findById(currentUser.getId());
        if (user != null) {
            user.setUsedBytes(Math.max(0, user.getUsedBytes() - file.getFileSizeBytes()));
            userDao.update(user);
            // Mevcut session'daki currentUser nesnesini de güncelle (Controller'daki nesne)
            currentUser.setUsedBytes(user.getUsedBytes());
        }

        fileItemDao.delete(file);
    }

    @Transactional(readOnly = false)
    public void toggleStar(Long fileId, User currentUser) {
        FileItem file = fileItemDao.findById(fileId);
        if (file == null)
            throw new FileNotFoundException("Dosya bulunamadı");
        if (!file.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu dosyaya erişim yetkiniz yok");

        file.setStarred(!file.isStarred());
    }

    @Transactional(readOnly = false)
    public void touchFile(Long fileId, User currentUser) {
        FileItem file = fileItemDao.findById(fileId);
        if (file != null && file.getOwner().getId().equals(currentUser.getId())) {
            file.setUpdatedAt(new java.util.Date());
            fileItemDao.update(file);
        }
    }

    public List<FileItem> getStarredFiles(User owner) {
        return fileItemDao.findStarredByOwner(owner);
    }

    public FileItem getFileById(Long fileId, User currentUser) {
        FileItem file = fileItemDao.findById(fileId);
        if (file == null)
            throw new FileNotFoundException("Dosya bulunamadı");
        if (!file.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu dosyaya erişim yetkiniz yok");
        return file;
    }

    public List<FileItem> getFilesInTrash(Folder folder, User owner) {
        return fileItemDao.findByFolderAndOwnerInTrash(folder, owner);
    }

    public List<FileItem> getRecentFiles(User owner) {
        return fileItemDao.findRecentByOwner(owner, 5);
    }
}
