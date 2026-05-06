package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FileItemDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private FileItemDao fileItemDao;

    @InjectMocks
    private FileService fileService;

    // --- ÖZELLİK 1: BAŞARILI DOSYA YÜKLEME ---
    @Test
    public void testUploadFile_BasariliYukleme() throws Exception {
        User user = new User();
        user.setUploadLimitBytes(1000L); // 1000 byte limit
        user.setUsedBytes(500L);         // 500 byte dolu

        FileItem fileItem = new FileItem();
        fileItem.setFileSizeBytes(200L); // 200 byte yüklenecek (Yer var)

        fileService.uploadFile(fileItem, user);

        // Kullanıcının kotası 500 + 200 = 700 byte'a güncellenmiş olmalı
        Assertions.assertEquals(700L, user.getUsedBytes());
        // Veritabanına kayıt işlemi (save) tam 1 kere çağrılmış olmalı
        Mockito.verify(fileItemDao, Mockito.times(1)).save(fileItem);
    }

    // --- ÖZELLİK 2: KOTA AŞIMI DURUMUNDA YÜKLEME ---
    @Test
    public void testUploadFile_KotaAsimi_HataFirlatmali() {
        User user = new User();
        user.setUploadLimitBytes(1000L); // 1000 byte limit
        user.setUsedBytes(900L);         // 900 byte dolu

        FileItem fileItem = new FileItem();
        fileItem.setFileSizeBytes(500L); // 500 byte yüklenmek isteniyor (Yer yok!)

        // Kota aşımı olacağı için Exception fırlatmasını bekliyoruz
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            fileService.uploadFile(fileItem, user);
        });

        Assertions.assertEquals("Kota aşıldı! Mevcut alanınız bu dosya için yetersiz.", exception.getMessage());
        // Hata fırladığı için veritabanına KAYIT YAPILMAMIŞ olmalı (0 times)
        Mockito.verify(fileItemDao, Mockito.times(0)).save(Mockito.any());
    }

    // --- ÖZELLİK 3: DOSYA TAŞIMA ---
    @Test
    public void testMoveFile_BasariliTasima() {
        Folder targetFolder = new Folder();
        targetFolder.setId(5L);

        FileItem fileItem = new FileItem();
        fileItem.setId(10L);

        // DAO'dan id'si 10 olan dosya istendiğinde bu sahte dosyayı dön
        Mockito.when(fileItemDao.findById(10L)).thenReturn(fileItem);

        fileService.moveFile(10L, targetFolder);

        // Dosyanın yeni klasörü targetFolder (Id: 5) olarak güncellenmiş olmalı
        Assertions.assertEquals(5L, fileItem.getFolder().getId());
    }

    // --- ÖZELLİK 4: BAŞARILI DOSYA KOPYALAMA ---
    @Test
    public void testCopyFile_BasariliKopyalama() throws Exception {
        User user = new User();
        user.setUploadLimitBytes(1000L);
        user.setUsedBytes(100L); // Çok yer var

        FileItem originalFile = new FileItem();
        originalFile.setId(10L);
        originalFile.setOriginalName("rapor.pdf");
        originalFile.setFileSizeBytes(100L);

        Folder targetFolder = new Folder();

        Mockito.when(fileItemDao.findById(10L)).thenReturn(originalFile);

        fileService.copyFile(10L, targetFolder, user);

        // Kullanıcının kotası kopya dosya boyutu kadar (100+100=200) artmış olmalı
        Assertions.assertEquals(200L, user.getUsedBytes());
        // Veritabanına yeni bir dosya olarak kaydedilmiş olmalı
        Mockito.verify(fileItemDao, Mockito.times(1)).save(Mockito.any(FileItem.class));
    }

    // --- ÖZELLİK 5: KOTA AŞIMI DURUMUNDA KOPYALAMA ---
    @Test
    public void testCopyFile_KotaAsimi_HataFirlatmali() {
        User user = new User();
        user.setUploadLimitBytes(1000L);
        user.setUsedBytes(950L); // Yer yok

        FileItem originalFile = new FileItem();
        originalFile.setId(10L);
        originalFile.setFileSizeBytes(100L); // 100 byte kopyalanacak (Aşar)

        Mockito.when(fileItemDao.findById(10L)).thenReturn(originalFile);

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            fileService.copyFile(10L, new Folder(), user);
        });

        Assertions.assertEquals("Kota aşıldı! Kopya işlemi için yeterli alan yok.", exception.getMessage());
    }

    // --- ÖZELLİK 6: ÇÖPE ATMA (SOFT DELETE) ---
    @Test
    public void testSoftDeleteFile() {
        FileItem fileItem = new FileItem();
        fileItem.setId(1L);
        fileItem.setIsDeleted(false); // Başlangıçta silinmemiş

        Mockito.when(fileItemDao.findById(1L)).thenReturn(fileItem);

        fileService.softDeleteFile(1L);

        // DAO'daki soft delete metodunun çağrılmış olması gerekiyor
        Mockito.verify(fileItemDao, Mockito.times(1)).softDelete(fileItem);
    }
}