package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FileItemDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import java.util.List;

@Service
@Transactional
public class FileService {

    @Autowired
    private FileItemDao fileItemDao;

    // 1. Dosyaları Getirme ve Arama
    public List<FileItem> getUserFiles(Folder folder, User owner) {
        return fileItemDao.findFilesByFolderAndOwner(folder, owner);
    }

    public List<FileItem> searchUserFiles(User owner, String keyword, String mimeType) {
        return fileItemDao.searchFiles(owner, keyword, mimeType);
    }

    // 2. Upload ve Kota Kontrolü (Yol Haritası Zorunlu İsteri)
    public void uploadFile(FileItem fileItem, User user) throws Exception {
        if (user.isLimitExceeded(fileItem.getFileSizeBytes())) {
            throw new Exception("Kota aşıldı! Mevcut alanınız bu dosya için yetersiz.");
        }
        // Hibernate session açık olduğu için user'ın byte'ını artırdığımızda DB'ye otomatik yansır.
        user.setUsedBytes(user.getUsedBytes() + fileItem.getFileSizeBytes());
        fileItemDao.save(fileItem);
    }

    // 3. Dosya Taşıma (Yol Haritası Zorunlu İsteri)
    public void moveFile(Long fileId, Folder targetFolder) {
        FileItem file = fileItemDao.findById(fileId);
        if(file != null) {
            file.setFolder(targetFolder);
            // JPA/Hibernate'de merge veya persist çağırmaya bile gerek yok,
            // @Transactional metot bittiğinde değişiklikleri DB'ye kendi yazar.
        }
    }

    // 4. Dosya Kopyalama (Yol Haritası Zorunlu İsteri)
    public void copyFile(Long fileId, Folder targetFolder, User user) throws Exception {
        FileItem original = fileItemDao.findById(fileId);
        if (original == null) return;

        // Kopya dosya da yer kaplayacağı için KOTA KONTROLÜ tekrar yapılmalı!
        if (user.isLimitExceeded(original.getFileSizeBytes())) {
            throw new Exception("Kota aşıldı! Kopya işlemi için yeterli alan yok.");
        }

        FileItem copy = new FileItem();
        copy.setOriginalName("Kopya_" + original.getOriginalName());
        copy.setStoredName(java.util.UUID.randomUUID().toString()); // Yeni fiziksel isim
        copy.setStoragePath(original.getStoragePath()); // Şimdilik aynı yolu gösteriyor
        copy.setFileSizeBytes(original.getFileSizeBytes());
        copy.setMimeType(original.getMimeType());
        copy.setFolder(targetFolder);
        copy.setOwner(user);

        // Kullanıcının kotasını güncelle ve yeni dosyayı kaydet
        user.setUsedBytes(user.getUsedBytes() + copy.getFileSizeBytes());
        fileItemDao.save(copy);
    }

    // 5. Soft Delete İşlemi
    public void softDeleteFile(Long fileId) {
        FileItem file = fileItemDao.findById(fileId);
        if(file != null) {
            fileItemDao.softDelete(file);
        }
    }
}