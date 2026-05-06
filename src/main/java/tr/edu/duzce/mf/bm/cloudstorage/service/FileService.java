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

    // Kullanıcının bulunduğu klasördeki dosyaları getir
    public List<FileItem> getUserFiles(Folder folder, User owner) {
        return fileItemDao.findFilesByFolderAndOwner(folder, owner);
    }

    // Dosya Yükleme ve Kota Kontrolü (İş Mantığı)
    public void uploadFile(FileItem fileItem, User user) throws Exception {

        // 1. Kural: Kullanıcının kotası bu dosyayı almaya yetiyor mu?
        if (user.isLimitExceeded(fileItem.getFileSizeBytes())) {
            throw new Exception("Kota aşıldı! Mevcut alanınız bu dosya için yetersiz.");
        }

        // 2. Kural: Her şey yolundaysa kullanıcının "Kullanılan Alan" miktarını güncelle
        user.setUsedBytes(user.getUsedBytes() + fileItem.getFileSizeBytes());

        // 3. Dosyayı veritabanına kaydet (Diske yazma işlemi Controller'da yapılacak)
        fileItemDao.save(fileItem);
    }
}