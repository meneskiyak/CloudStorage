package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FileItemDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FolderDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import java.util.Date;
import java.util.List;

@Component
public class TrashCleanupTask {

    private static final long THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000;

    @Autowired private FileItemDao fileItemDao;
    @Autowired private FolderDao folderDao;
    @Autowired private UserDao userDao;
    @Autowired private MinioService minioService;

    // Her gece 02:00'de çalışır
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeExpiredItems() {
        Date cutoff = new Date(System.currentTimeMillis() - THIRTY_DAYS_MS);

        deleteExpiredFiles(cutoff);
        deleteExpiredFolders(cutoff);
    }

    private void deleteExpiredFiles(Date cutoff) {
        List<FileItem> expired = fileItemDao.findExpiredFromTrash(cutoff);
        for (FileItem file : expired) {
            try {
                minioService.deleteFile(file.getStoredName());
            } catch (Exception ignored) {}

            User owner = userDao.findById(file.getOwner().getId());
            if (owner != null) {
                owner.setUsedBytes(Math.max(0, owner.getUsedBytes() - file.getFileSizeBytes()));
                userDao.update(owner);
            }

            fileItemDao.delete(file);
        }
    }

    private void deleteExpiredFolders(Date cutoff) {
        List<Folder> expired = folderDao.findExpiredFromTrash(cutoff);
        for (Folder folder : expired) {
            long size = calculateFolderSize(folder);
            recursiveDelete(folder);

            User owner = userDao.findById(folder.getOwner().getId());
            if (owner != null) {
                owner.setUsedBytes(Math.max(0, owner.getUsedBytes() - size));
                userDao.update(owner);
            }

            folderDao.delete(folder);
        }
    }

    private long calculateFolderSize(Folder folder) {
        long size = 0;
        for (FileItem f : folder.getFiles()) size += f.getFileSizeBytes();
        for (Folder sub : folder.getChildren()) size += calculateFolderSize(sub);
        return size;
    }

    private void recursiveDelete(Folder folder) {
        for (FileItem file : folder.getFiles()) {
            try { minioService.deleteFile(file.getStoredName()); } catch (Exception ignored) {}
            fileItemDao.delete(file);
        }
        for (Folder sub : folder.getChildren()) {
            recursiveDelete(sub);
            folderDao.delete(sub);
        }
    }
}
