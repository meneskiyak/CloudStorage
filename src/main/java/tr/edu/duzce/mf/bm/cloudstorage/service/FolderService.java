package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.AccessDeniedException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderAlreadyExistsException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderNotFoundException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.StorageQuotaExceededException;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FolderDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FileItemDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FolderService {

    @Autowired
    private FolderDao folderDao;

    @Autowired
    private FileItemDao fileItemDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private MinioService minioService;

    // Klasör Oluşturma
    public void createFolder(Folder folder) {
        // Üst klasör kontrolleri
        if (folder.getParent() != null) {
            Folder parent = folder.getParent();
            if (!parent.getOwner().getId().equals(folder.getOwner().getId()))
                throw new AccessDeniedException("Üst klasöre erişim yetkiniz yok!");
            
            if (parent.isDeleted())
                throw new FolderNotFoundException("Silinmiş bir klasör içinde yeni klasör oluşturulamaz!");
        }

        List<Folder> folderList = folderDao.findByParentAndOwner(
                folder.getParent(),
                folder.getOwner()
        );

        boolean exist = folderList.stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(folder.getName()));

        if (exist)
            throw new FolderAlreadyExistsException("Bu isimde bir klasör zaten var");

        folderDao.save(folder);
    }

    // Alt Klasörleri Getirme
    public List<Folder> getSubFolders(Folder parent, User owner) {
        return folderDao.findByParentAndOwner(parent, owner);
    }

    public Folder getFolder(Long id) {
        Folder folder = folderDao.findById(id);
        if (folder == null)
            throw new FolderNotFoundException("Klasör bulunamadı");
        return folder;
    }

    // Klasör Taşıma (Parent'ı Değiştirme)
    public void moveFolder(Long folderId, Folder newParent, User currentUser) {
        Folder folder = folderDao.findById(folderId);
        if (folder == null)
            throw new FolderNotFoundException("Taşınacak klasör bulunamadı!");
        
        if (!folder.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu klasörü taşıma yetkiniz yok!");

        if (newParent != null) {
            // Hedef klasör sahiplik ve silinmişlik kontrolü
            if (!newParent.getOwner().getId().equals(currentUser.getId()))
                throw new AccessDeniedException("Hedef klasöre erişim yetkiniz yok!");
            
            if (newParent.isDeleted())
                throw new FolderNotFoundException("Silinmiş bir klasöre taşıma yapılamaz!");

            // Sonsuz döngü (Circular Reference) kontrolü
            if (isDescendant(folder, newParent)) {
                throw new IllegalArgumentException("Bir klasör, kendi alt klasörlerinden birine taşınamaz!");
            }
            
            // Kendine taşıma kontrolü
            if (folder.getId().equals(newParent.getId())) {
                throw new IllegalArgumentException("Bir klasör kendi içine taşınamaz!");
            }
        }

        folder.setParent(newParent);
    }

    /**
     * Bir klasörün, başka bir klasörün alt öğesi (torunu) olup olmadığını kontrol eder.
     */
    private boolean isDescendant(Folder folder, Folder potentialDescendant) {
        Folder current = potentialDescendant;
        while (current != null) {
            if (current.getId().equals(folder.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    // Klasör yükleme — webkitRelativePath'leri parse ederek hiyerarşiyi oluşturur
    public void uploadFolder(List<MultipartFile> files, List<String> relativePaths,
                             Folder parentFolder, User owner) {

        // Üst klasör kontrolleri
        if (parentFolder != null) {
            if (!parentFolder.getOwner().getId().equals(owner.getId()))
                throw new AccessDeniedException("Hedef klasöre erişim yetkiniz yok!");
            
            if (parentFolder.isDeleted())
                throw new FolderNotFoundException("Silinmiş bir klasöre yükleme yapılamaz!");
        }

        // Kota kontrolü — toplam boyutu hesapla
        long totalSize = files.stream().filter(f -> !f.isEmpty()).mapToLong(MultipartFile::getSize).sum();
        if (owner.isLimitExceeded(totalSize)) {
            throw new StorageQuotaExceededException(
                    "Kotanız bu klasörü yüklemek için yetersiz! (Gereken: " + totalSize + " byte)"
            );
        }

        // path -> Folder cache: aynı klasörü iki kez oluşturmamak için
        Map<String, Folder> folderCache = new HashMap<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String relativePath = relativePaths.get(i); // örn: "photos/vacation/img.jpg"

            String[] parts = relativePath.split("/");
            // Son eleman dosya adı, öncekiler klasör segmentleri
            Folder currentParent = parentFolder;

            for (int j = 0; j < parts.length - 1; j++) {
                String segmentName = parts[j];
                // Cache key: parent id (null ise "root") + segment adı
                String cacheKey = (currentParent != null ? currentParent.getId() : "root") + "/" + segmentName;

                if (!folderCache.containsKey(cacheKey)) {
                    // Bu segment daha önce oluşturulmadıysa oluştur
                    Folder segment = new Folder();
                    segment.setName(segmentName);
                    segment.setParent(currentParent);
                    segment.setOwner(owner);
                    folderDao.save(segment);
                    folderCache.put(cacheKey, segment);
                }
                currentParent = folderCache.get(cacheKey);
            }

            // Dosyayı MinIO'ya yükle ve DB'ye kaydet
            if (!file.isEmpty()) {
                try {
                    String storedName = minioService.uploadFile(file);

                    FileItem fileItem = new FileItem();
                    String[] pathParts = relativePath.split("/");
                    fileItem.setOriginalName(pathParts[pathParts.length - 1]);
                    fileItem.setStoredName(storedName);
                    fileItem.setStoragePath(minioService.bucket);
                    fileItem.setFileSizeBytes(file.getSize());
                    fileItem.setMimeType(file.getContentType() != null
                            ? file.getContentType() : "application/octet-stream");
                    fileItem.setFolder(currentParent);
                    fileItem.setOwner(owner);
                    fileItemDao.save(fileItem);

                    owner.setUsedBytes(owner.getUsedBytes() + file.getSize());
                } catch (Exception e) {
                    throw new tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.StorageException("Dosya yüklenemedi: " + file.getOriginalFilename(), e);
                }
            }
        }
        userDao.update(owner);
    }

    public void deleteFolder(Long folderId) throws Exception {
        Folder folder = folderDao.findById(folderId);
        if (folder == null) {
            throw new Exception("Klasör bulunamadı!");
        }

        // Eğer klasör boş değilse silme gibi ek kontroller buraya yazılabilir
        folderDao.delete(folder);
    }

    @Transactional(readOnly = false)
    public void softDeleteFolder(Long folderId, User currentUser) {
        Folder folder = folderDao.findById(folderId);
        if (folder == null)
            throw new FolderNotFoundException("Klasör bulunamadı");
        if (!folder.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu klasöre erişim yetkiniz yok");

        // Klasör içindeki tüm dosyaları bul ve boyutlarını kotadan düş
        long totalSize = calculateFolderSize(folder);
        currentUser.setUsedBytes(Math.max(0, currentUser.getUsedBytes() - totalSize));
        userDao.update(currentUser);

        // Klasörü ve tüm alt öğelerini işaretle
        recursiveSoftDelete(folder);
    }

    private long calculateFolderSize(Folder folder) {
        long size = 0;
        for (FileItem file : folder.getFiles()) {
            if (!file.isDeleted()) {
                size += file.getFileSizeBytes();
            }
        }
        for (Folder sub : folder.getChildren()) {
            if (!sub.isDeleted()) {
                size += calculateFolderSize(sub);
            }
        }
        return size;
    }

    private void recursiveSoftDelete(Folder folder) {
        folder.setDeleted(true);
        for (FileItem file : folder.getFiles()) {
            file.setDeleted(true);
        }
        for (Folder sub : folder.getChildren()) {
            recursiveSoftDelete(sub);
        }
    }

    public List<Folder> getDeletedFolders(User owner) {
        return folderDao.findDeletedByOwner(owner);
    }

    @Transactional(readOnly = false)
    public void restoreFolder(Long folderId, User currentUser) {
        Folder folder = folderDao.findById(folderId);
        if (folder == null)
            throw new FolderNotFoundException("Klasör bulunamadı");
        if (!folder.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu klasöre erişim yetkiniz yok");

        // Geri yükleme için kota kontrolü
        long totalSize = calculateDeletedFolderSize(folder);
        if (currentUser.isLimitExceeded(totalSize)) {
            throw new StorageQuotaExceededException(
                    "Klasörü geri yüklemek için yeterli alanınız yok!"
            );
        }

        currentUser.setUsedBytes(currentUser.getUsedBytes() + totalSize);
        userDao.update(currentUser);

        recursiveRestore(folder);
    }

    private long calculateDeletedFolderSize(Folder folder) {
        long size = 0;
        for (FileItem file : folder.getFiles()) {
            if (file.isDeleted()) {
                size += file.getFileSizeBytes();
            }
        }
        for (Folder sub : folder.getChildren()) {
            if (sub.isDeleted()) {
                size += calculateDeletedFolderSize(sub);
            }
        }
        return size;
    }

    private void recursiveRestore(Folder folder) {
        folder.setDeleted(false);
        for (FileItem file : folder.getFiles()) {
            file.setDeleted(false);
        }
        for (Folder sub : folder.getChildren()) {
            recursiveRestore(sub);
        }
    }

    @Transactional(readOnly = false)
    public void permanentlyDeleteFolder(Long folderId, User currentUser) {
        Folder folder = folderDao.findById(folderId);
        if (folder == null)
            throw new FolderNotFoundException("Klasör bulunamadı");
        if (!folder.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu klasöre erişim yetkiniz yok");

        // Önce MinIO'daki dosyaları temizle
        recursivePermanentDelete(folder);
        
        // Sonra klasörü DB'den sil
        folderDao.delete(folder);
    }

    private void recursivePermanentDelete(Folder folder) {
        for (FileItem file : folder.getFiles()) {
            try {
                minioService.deleteFile(file.getStoredName());
            } catch (Exception e) {
                // Hata loglanabilir
            }
        }
        for (Folder sub : folder.getChildren()) {
            recursivePermanentDelete(sub);
        }
    }

    @Transactional(readOnly = false)
    public void renameFolder(Long folderId, String newName, User currentUser) {
        Folder folder = folderDao.findById(folderId);
        if (folder == null)
            throw new FolderNotFoundException("Klasör bulunamadı");
        if (!folder.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu klasöre erişim yetkiniz yok");

        List<Folder> folderList = folderDao.findByParentAndOwner(folder.getParent(), currentUser);
        boolean exist = folderList.stream()
                .anyMatch(f -> !f.getId().equals(folderId) && f.getName().equalsIgnoreCase(newName));

        if (exist)
            throw new FolderAlreadyExistsException("Bu isimde bir klasör zaten var");

        folder.setName(newName);
    }

    @Transactional(readOnly = false)
    public void toggleStar(Long folderId, User currentUser) {
        Folder folder = folderDao.findById(folderId);
        if (folder == null)
            throw new FolderNotFoundException("Klasör bulunamadı");
        if (!folder.getOwner().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Bu klasöre erişim yetkiniz yok");

        folder.setStarred(!folder.isStarred());
    }

    public List<Folder> getStarredFolders(User owner) {
        return folderDao.findStarredByOwner(owner);
    }

    public List<Folder> searchFolders(User owner, String keyword) {
        return folderDao.searchByName(keyword, owner);
    }
}