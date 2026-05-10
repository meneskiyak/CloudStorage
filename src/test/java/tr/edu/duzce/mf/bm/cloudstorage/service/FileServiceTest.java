package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private FileItemDao fileItemDao;

    @Mock
    private UserDao userDao;

    @Mock
    private MinioService minioService;

    @Mock
    private FolderDao folderDao;

    @InjectMocks
    private FileService fileService;

    private User testUser;
    private Folder testFolder;
    private FileItem testFile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setUploadLimitBytes(1024L * 1024L); // 1 MB
        testUser.setUsedBytes(0L);

        testFolder = new Folder();
        testFolder.setId(10L);
        testFolder.setName("My Folder");
        testFolder.setOwner(testUser);

        testFile = new FileItem();
        testFile.setId(100L);
        testFile.setOriginalName("test.txt");
        testFile.setFileSizeBytes(512L);
        testFile.setOwner(testUser);
        testFile.setFolder(testFolder);
    }

    @Test
    @DisplayName("Başarılı dosya yükleme ve kota güncelleme")
    void shouldUploadFileSuccessfully() throws Exception {
        // Dosya yükleme işlemi kotayı güncellemeli ve DB'ye kaydetmeli
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getOriginalFilename()).thenReturn("upload.txt");
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(minioService.uploadFile(any())).thenReturn("stored-name");

        fileService.uploadFile(multipartFile, null, testUser);

        verify(userDao).update(testUser);
        verify(fileItemDao).save(any(FileItem.class));
        assertEquals(100L, testUser.getUsedBytes());
    }

    @Test
    @DisplayName("Kota aşıldığında dosya yükleme engellenmeli")
    void shouldThrowExceptionWhenQuotaExceeded() {
        // Kullanıcı kotası yetersizse StorageQuotaExceededException fırlatmalı
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getSize()).thenReturn(2L * 1024 * 1024); // 2 MB (Limit 1 MB)

        assertThrows(StorageQuotaExceededException.class, () -> fileService.uploadFile(multipartFile, null, testUser));
    }

    @Test
    @DisplayName("Başkasına ait klasöre dosya yükleme engellenmeli")
    void shouldThrowExceptionWhenUploadingToOthersFolder() {
        // Başka bir kullanıcının klasörüne yükleme yapılmasına izin verilmemeli
        User otherUser = new User();
        otherUser.setId(2L);
        testFolder.setOwner(otherUser);

        when(folderDao.findById(10L)).thenReturn(testFolder);
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getSize()).thenReturn(100L);

        assertThrows(AccessDeniedException.class, () -> fileService.uploadFile(multipartFile, 10L, testUser));
    }

    @Test
    @DisplayName("Dosya çöp kutusuna taşınmalı (Soft Delete)")
    void shouldSoftDeleteFile() {
        // Dosya silindi olarak işaretlenmeli ancak DB'den kalkmamalı
        when(fileItemDao.findById(100L)).thenReturn(testFile);

        fileService.softDeleteFile(100L, testUser);

        verify(fileItemDao).softDelete(testFile);
    }

    @Test
    @DisplayName("Dosya kalıcı olarak silinmeli ve kota boşalmalı")
    void shouldPermanentlyDeleteFile() throws Exception {
        // Kalıcı silme işlemi MinIO'dan silmeli ve kullanıcı kotasını düşürmeli
        when(fileItemDao.findById(100L)).thenReturn(testFile);
        when(userDao.findById(1L)).thenReturn(testUser);
        testUser.setUsedBytes(1000L);

        fileService.permanentlyDeleteFile(100L, testUser);

        verify(minioService).deleteFile(testFile.getStoredName());
        verify(fileItemDao).delete(testFile);
        assertEquals(1000L - 512L, testUser.getUsedBytes());
    }

    @Test
    @DisplayName("Dosya geri yüklenmeli")
    void shouldRestoreFile() {
        // Çöp kutusundaki dosya geri yüklenebilmeli
        testFile.setDeleted(true);
        when(fileItemDao.findById(100L)).thenReturn(testFile);

        fileService.restoreFile(100L, testUser);

        assertFalse(testFile.isDeleted());
    }

    @Test
    @DisplayName("Dosya başka bir klasöre taşınmalı")
    void shouldMoveFile() {
        // Dosyanın klasörü (parent) güncellenebilmeli
        Folder targetFolder = new Folder();
        targetFolder.setId(20L);
        targetFolder.setOwner(testUser);

        when(fileItemDao.findById(100L)).thenReturn(testFile);

        fileService.moveFile(100L, targetFolder, testUser);

        assertEquals(targetFolder, testFile.getFolder());
    }

    @Test
    @DisplayName("Dosya kopyalanmalı")
    void shouldCopyFile() throws Exception {
        // Dosya kopyalandığında yeni bir kayıt oluşturulmalı ve kota güncellenmeli
        when(fileItemDao.findById(100L)).thenReturn(testFile);

        fileService.copyFile(100L, testFolder, testUser);

        verify(fileItemDao).save(any(FileItem.class));
        verify(userDao).update(testUser);
        assertEquals(512L, testUser.getUsedBytes());
    }

    @Test
    @DisplayName("Yıldız durumu değiştirilmeli")
    void shouldToggleStar() {
        // Dosya yıldızlı/yıldızsız duruma getirilebilmeli
        when(fileItemDao.findById(100L)).thenReturn(testFile);
        assertFalse(testFile.isStarred());

        fileService.toggleStar(100L, testUser);
        assertTrue(testFile.isStarred());

        fileService.toggleStar(100L, testUser);
        assertFalse(testFile.isStarred());
    }

    @Test
    @DisplayName("Olmayan dosyaya erişim hata fırlatmalı")
    void shouldThrowExceptionWhenFileNotFound() {
        // Bulunmayan bir dosya üzerinde işlem yapıldığında FileNotFoundException fırlatmalı
        when(fileItemDao.findById(999L)).thenReturn(null);

        assertThrows(FileNotFoundException.class, () -> fileService.getFileById(999L, testUser));
    }
}
