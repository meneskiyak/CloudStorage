package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FolderDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import java.util.List;

@Service
@Transactional
public class FolderService {

    @Autowired
    private FolderDao folderDao;

    // Klasör Oluşturma
    public void createFolder(Folder folder) {
        folderDao.save(folder);
    }

    // Alt Klasörleri Getirme
    public List<Folder> getSubFolders(Folder parent, User owner) {
        return folderDao.findByParentAndOwner(parent, owner);
    }

    public Folder getFolderById(Long id) {
        return folderDao.findById(id);
    }

    // Klasör Taşıma (Parent'ı Değiştirme)
    public void moveFolder(Long folderId, Folder newParent) {
        Folder folder = folderDao.findById(folderId);
        if(folder != null) {
            folder.setParent(newParent);
        }
    }

    public void deleteFolder(Long folderId) throws Exception {
        Folder folder = folderDao.findById(folderId);
        if (folder == null) {
            throw new Exception("Klasör bulunamadı!");
        }

        // Eğer klasör boş değilse silme gibi ek kontroller buraya yazılabilir
        folderDao.delete(folder);
    }
}