package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.AccessDeniedException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderAlreadyExistsException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderNotFoundException;
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

    @Autowired
    private EmbeddingService embeddingService;

    // Klasör Oluşturma
    public void createFolder(Folder folder) {

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
    public void moveFolder(Long folderId, Folder newParent) {
        Folder folder = folderDao.findById(folderId);
        if(folder != null) {
            folder.setParent(newParent);
        }
    }

    // Klasör yükleme — webkitRelativePath'leri parse ederek hiyerarşiyi oluşturur
    public void uploadFolder(List<MultipartFile> files, List<String> relativePaths,
                             Folder parentFolder, User owner) {
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

                    embeddingService.embedPdf(fileItem.getId(), owner.getId(),
                            storedName, fileItem.getOriginalName(), fileItem.getMimeType());
                } catch (Exception e) {
                    throw new RuntimeException("Dosya yüklenemedi: " + file.getOriginalFilename(), e);
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

        folderDao.softDelete(folder);
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

        folder.setDeleted(false);
    }

    @Transactional(readOnly = false)
    public void permanentlyDeleteFolder(Long folderId, User currentUser) {
        Folder folder = folderDao.findById(folderId);
        if (folder == null)
            throw new FolderNotFoundException("Klasör bulunamadı");
        if (!folder.getOwner().getId().equals(currentUser.getId()))
            throw new tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.AccessDeniedException("Bu klasöre erişim yetkiniz yok");

        // Klasör içindeki dosyaların vektörlerini de silmek iyi olurdu, 
        // ancak recursive yapı karmaşık olduğu için şimdilik temel işlevlere odaklanıyoruz.
        folderDao.delete(folder);
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
