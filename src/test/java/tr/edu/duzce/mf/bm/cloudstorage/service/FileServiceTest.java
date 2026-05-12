package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.AccessDeniedException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FileNotFoundException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderNotFoundException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.StorageException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.StorageQuotaExceededException;
import tr.edu.duzce.mf.bm.cloudstorage.core.enums.Role;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FileItemDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FolderDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import java.util.List;

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
    private User otherUser;
    private Folder testFolder;
    private FileItem testFile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setUploadLimitBytes(1024L * 1024L); // 1 MB
        testUser.setUsedBytes(0L);
        testUser.setRole(Role.USER);

        otherUser = new User();
        otherUser.setId(2L);

        testFolder = new Folder();
        testFolder.setId(10L);
        testFolder.setName("My Folder");
        testFolder.setOwner(testUser);
        testFolder.setDeleted(false);

        testFile = new FileItem();
        testFile.setId(100L);
        testFile.setOriginalName("test.txt");
        testFile.setStoredName("uuid_test.txt");
        testFile.setFileSizeBytes(512L);
        testFile.setMimeType("text/plain");
        testFile.setOwner(testUser);
        testFile.setFolder(testFolder);
        testFile.setDeleted(false);
        testFile.setStarred(false);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // uploadFile
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("uploadFile")
    class UploadFile {

        @Test
        @DisplayName("Kota aşıldığında StorageQuotaExceededException")
        void quotaExceeded_throws() {
            MultipartFile file = mockMultipart("big.zip", 2L * 1024 * 1024);

            assertThrows(StorageQuotaExceededException.class,
                    () -> fileService.uploadFile(file, null, testUser));
            verifyNoInteractions(minioService, fileItemDao, userDao);
        }

        @Test
        @DisplayName("Hedef klasör bulunamazsa FolderNotFoundException")
        void folderNotFound_throws() {
            when(folderDao.findById(99L)).thenReturn(null);
            MultipartFile file = mockMultipart("f.txt", 100L);

            assertThrows(FolderNotFoundException.class,
                    () -> fileService.uploadFile(file, 99L, testUser));
        }

        @Test
        @DisplayName("Başkasının klasörüne yükleme — AccessDeniedException")
        void uploadToOthersFolder_throws() {
            testFolder.setOwner(otherUser);
            when(folderDao.findById(10L)).thenReturn(testFolder);
            MultipartFile file = mockMultipart("f.txt", 100L);

            assertThrows(AccessDeniedException.class,
                    () -> fileService.uploadFile(file, 10L, testUser));
        }

        @Test
        @DisplayName("Silinmiş klasöre yükleme — FolderNotFoundException")
        void uploadToDeletedFolder_throws() {
            testFolder.setDeleted(true);
            when(folderDao.findById(10L)).thenReturn(testFolder);
            MultipartFile file = mockMultipart("f.txt", 100L);

            assertThrows(FolderNotFoundException.class,
                    () -> fileService.uploadFile(file, 10L, testUser));
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // moveFile
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("moveFile")
        class MoveFile {

            @Test
            @DisplayName("Başarılı taşıma — klasör güncellenmeli")
            void success() {
                Folder target = folder(20L, testUser, false);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                fileService.moveFile(100L, target, testUser);

                assertEquals(target, testFile.getFolder());
            }

            @Test
            @DisplayName("null hedef klasör — root'a taşıma geçerli")
            void moveToRoot_success() {
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                fileService.moveFile(100L, null, testUser);

                assertNull(testFile.getFolder());
            }

            @Test
            @DisplayName("Dosya bulunamazsa FileNotFoundException")
            void fileNotFound_throws() {
                when(fileItemDao.findById(999L)).thenReturn(null);

                assertThrows(FileNotFoundException.class,
                        () -> fileService.moveFile(999L, testFolder, testUser));
            }

            @Test
            @DisplayName("Başkasının dosyası — AccessDeniedException")
            void othersFile_throws() {
                testFile.setOwner(otherUser);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(AccessDeniedException.class,
                        () -> fileService.moveFile(100L, testFolder, testUser));
            }

            @Test
            @DisplayName("Hedef başkasının klasörü — AccessDeniedException")
            void targetOthersFolder_throws() {
                Folder target = folder(20L, otherUser, false);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(AccessDeniedException.class,
                        () -> fileService.moveFile(100L, target, testUser));
            }

            @Test
            @DisplayName("Hedef silinmiş klasör — FolderNotFoundException")
            void targetDeletedFolder_throws() {
                Folder target = folder(20L, testUser, true);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(FolderNotFoundException.class,
                        () -> fileService.moveFile(100L, target, testUser));
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // copyFile
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("copyFile")
        class CopyFile {

            @Test
            @DisplayName("Başarılı kopyalama — yeni kayıt ve kota güncellenmeli")
            void success() throws Exception {
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                fileService.copyFile(100L, testFolder, testUser);

                verify(minioService).copyFile(eq("uuid_test.txt"), anyString());
                verify(fileItemDao).save(argThat(f -> f.getOriginalName()
                        .startsWith("Kopya_") && f.getFileSizeBytes() == 512L));
                verify(userDao).update(testUser);
                assertEquals(512L, testUser.getUsedBytes());
            }

            @Test
            @DisplayName("Dosya bulunamazsa FileNotFoundException")
            void fileNotFound_throws() {
                when(fileItemDao.findById(999L)).thenReturn(null);

                assertThrows(FileNotFoundException.class,
                        () -> fileService.copyFile(999L, testFolder, testUser));
            }

            @Test
            @DisplayName("Başkasının dosyası — AccessDeniedException")
            void othersFile_throws() {
                testFile.setOwner(otherUser);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(AccessDeniedException.class,
                        () -> fileService.copyFile(100L, testFolder, testUser));
            }

            @Test
            @DisplayName("Kopyalama için kota yetersiz — StorageQuotaExceededException")
            void quotaExceeded_throws() {
                testUser.setUsedBytes(testUser.getUploadLimitBytes()); // dolu
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(StorageQuotaExceededException.class,
                        () -> fileService.copyFile(100L, testFolder, testUser));
            }

            @Test
            @DisplayName("Hedef silinmiş klasör — FolderNotFoundException")
            void targetDeletedFolder_throws() {
                Folder target = folder(20L, testUser, true);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(FolderNotFoundException.class,
                        () -> fileService.copyFile(100L, target, testUser));
            }

            @Test
            @DisplayName("MinIO kopyalama hatası — StorageException")
            void minioFails_throwsStorageException() throws Exception {
                when(fileItemDao.findById(100L)).thenReturn(testFile);
                doThrow(new RuntimeException("MinIO error")).when(minioService)
                        .copyFile(anyString(), anyString());

                assertThrows(StorageException.class,
                        () -> fileService.copyFile(100L, testFolder, testUser));
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // softDeleteFile
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("softDeleteFile")
        class SoftDeleteFile {

            @Test
            @DisplayName("Başarılı soft delete — softDelete çağrılmalı")
            void success() {
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                fileService.softDeleteFile(100L, testUser);

                verify(fileItemDao).softDelete(testFile);
                verifyNoInteractions(minioService);
            }

            @Test
            @DisplayName("Dosya bulunamazsa FileNotFoundException")
            void fileNotFound_throws() {
                when(fileItemDao.findById(100L)).thenReturn(null);

                assertThrows(FileNotFoundException.class,
                        () -> fileService.softDeleteFile(100L, testUser));
            }

            @Test
            @DisplayName("Başkasının dosyası — AccessDeniedException")
            void othersFile_throws() {
                testFile.setOwner(otherUser);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(AccessDeniedException.class,
                        () -> fileService.softDeleteFile(100L, testUser));
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // permanentlyDeleteFile
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("permanentlyDeleteFile")
        class PermanentlyDeleteFile {

            @Test
            @DisplayName("Başarılı kalıcı silme — MinIO, kota ve DB güncellenmeli")
            void success() throws Exception {
                testUser.setUsedBytes(1000L);
                when(fileItemDao.findById(100L)).thenReturn(testFile);
                when(userDao.findById(1L)).thenReturn(testUser);

                fileService.permanentlyDeleteFile(100L, testUser);

                verify(minioService).deleteFile("uuid_test.txt");
                verify(fileItemDao).delete(testFile);
                assertEquals(1000L - 512L, testUser.getUsedBytes());
            }

            @Test
            @DisplayName("MinIO hatası sessizce yutulmalı — DB silme yine de çalışmalı")
            void minioFails_dbStillDeletes() throws Exception {
                when(fileItemDao.findById(100L)).thenReturn(testFile);
                when(userDao.findById(1L)).thenReturn(testUser);
                doThrow(new RuntimeException("MinIO down")).when(minioService)
                        .deleteFile(anyString());

                // exception dışarı sızmamalı
                assertDoesNotThrow(() -> fileService.permanentlyDeleteFile(100L, testUser));
                verify(fileItemDao).delete(testFile);
            }

            @Test
            @DisplayName("Kota sıfırın altına düşmemeli")
            void usedBytes_neverGoesNegative() throws Exception {
                testUser.setUsedBytes(0L);
                when(fileItemDao.findById(100L)).thenReturn(testFile);
                when(userDao.findById(1L)).thenReturn(testUser);

                fileService.permanentlyDeleteFile(100L, testUser);

                assertEquals(0L, testUser.getUsedBytes());
            }

            @Test
            @DisplayName("Dosya bulunamazsa FileNotFoundException")
            void fileNotFound_throws() {
                when(fileItemDao.findById(100L)).thenReturn(null);

                assertThrows(FileNotFoundException.class,
                        () -> fileService.permanentlyDeleteFile(100L, testUser));
            }

            @Test
            @DisplayName("Başkasının dosyası — AccessDeniedException")
            void othersFile_throws() {
                testFile.setOwner(otherUser);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(AccessDeniedException.class,
                        () -> fileService.permanentlyDeleteFile(100L, testUser));
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // restoreFile
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("restoreFile")
        class RestoreFile {

            @Test
            @DisplayName("Başarılı geri yükleme — deleted false olmalı")
            void success() {
                testFile.setDeleted(true);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                fileService.restoreFile(100L, testUser);

                assertFalse(testFile.isDeleted());
            }

            @Test
            @DisplayName("Dosya bulunamazsa FileNotFoundException")
            void fileNotFound_throws() {
                when(fileItemDao.findById(100L)).thenReturn(null);

                assertThrows(FileNotFoundException.class,
                        () -> fileService.restoreFile(100L, testUser));
            }

            @Test
            @DisplayName("Başkasının dosyası — AccessDeniedException")
            void othersFile_throws() {
                testFile.setOwner(otherUser);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(AccessDeniedException.class,
                        () -> fileService.restoreFile(100L, testUser));
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // renameFile
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("renameFile")
        class RenameFile {

            @Test
            @DisplayName("Başarılı yeniden adlandırma")
            void success() {
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                fileService.renameFile(100L, "new-name.txt", testUser);

                assertEquals("new-name.txt", testFile.getOriginalName());
            }

            @Test
            @DisplayName("Dosya bulunamazsa FileNotFoundException")
            void fileNotFound_throws() {
                when(fileItemDao.findById(100L)).thenReturn(null);

                assertThrows(FileNotFoundException.class,
                        () -> fileService.renameFile(100L, "x.txt", testUser));
            }

            @Test
            @DisplayName("Başkasının dosyası — AccessDeniedException")
            void othersFile_throws() {
                testFile.setOwner(otherUser);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(AccessDeniedException.class,
                        () -> fileService.renameFile(100L, "x.txt", testUser));
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // toggleStar
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("toggleStar")
        class ToggleStar {

            @Test
            @DisplayName("Yıldız durumu iki kez toggle edilmeli")
            void togglesTwice() {
                when(fileItemDao.findById(100L)).thenReturn(testFile);
                assertFalse(testFile.isStarred());

                fileService.toggleStar(100L, testUser);
                assertTrue(testFile.isStarred());

                fileService.toggleStar(100L, testUser);
                assertFalse(testFile.isStarred());
            }

            @Test
            @DisplayName("Dosya bulunamazsa FileNotFoundException")
            void fileNotFound_throws() {
                when(fileItemDao.findById(100L)).thenReturn(null);

                assertThrows(FileNotFoundException.class,
                        () -> fileService.toggleStar(100L, testUser));
            }

            @Test
            @DisplayName("Başkasının dosyası — AccessDeniedException")
            void othersFile_throws() {
                testFile.setOwner(otherUser);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(AccessDeniedException.class,
                        () -> fileService.toggleStar(100L, testUser));
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // touchFile
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("touchFile")
        class TouchFile {

            @Test
            @DisplayName("Dosya sahibine ait — updatedAt ve update çağrılmalı")
            void success() {
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                fileService.touchFile(100L, testUser);

                assertNotNull(testFile.getUpdatedAt());
                verify(fileItemDao).update(testFile);
            }

            @Test
            @DisplayName("Dosya bulunamazsa hiçbir şey yapılmamalı")
            void fileNotFound_doesNothing() {
                when(fileItemDao.findById(100L)).thenReturn(null);

                assertDoesNotThrow(() -> fileService.touchFile(100L, testUser));
                verify(fileItemDao, never()).update(any());
            }

            @Test
            @DisplayName("Başkasının dosyası — hiçbir şey yapılmamalı")
            void othersFile_doesNothing() {
                testFile.setOwner(otherUser);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertDoesNotThrow(() -> fileService.touchFile(100L, testUser));
                verify(fileItemDao, never()).update(any());
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // getFileById
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("getFileById")
        class GetFileById {

            @Test
            @DisplayName("Başarılı — dosya döndürülmeli")
            void success() {
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                FileItem result = fileService.getFileById(100L, testUser);

                assertEquals(testFile, result);
            }

            @Test
            @DisplayName("Dosya bulunamazsa FileNotFoundException")
            void fileNotFound_throws() {
                when(fileItemDao.findById(999L)).thenReturn(null);

                assertThrows(FileNotFoundException.class,
                        () -> fileService.getFileById(999L, testUser));
            }

            @Test
            @DisplayName("Başkasının dosyası — AccessDeniedException")
            void othersFile_throws() {
                testFile.setOwner(otherUser);
                when(fileItemDao.findById(100L)).thenReturn(testFile);

                assertThrows(AccessDeniedException.class,
                        () -> fileService.getFileById(100L, testUser));
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // Query metodları
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("Query metodları")
        class QueryMethods {

            @Test
            @DisplayName("getUserFiles — DAO'ya delege etmeli")
            void getUserFiles_delegates() {
                when(fileItemDao.findFilesByFolderAndOwner(testFolder, testUser)).thenReturn(
                        List.of(testFile));

                List<FileItem> result = fileService.getUserFiles(testFolder, testUser);

                assertEquals(1, result.size());
                verify(fileItemDao).findFilesByFolderAndOwner(testFolder, testUser);
            }

            @Test
            @DisplayName("searchUserFiles — DAO'ya delege etmeli")
            void searchUserFiles_delegates() {
                when(fileItemDao.searchFiles(testUser, "test", "text/plain")).thenReturn(
                        List.of(testFile));

                List<FileItem> result = fileService.searchUserFiles(testUser, "test", "text/plain");

                assertEquals(1, result.size());
            }

            @Test
            @DisplayName("getDeletedFiles — DAO'ya delege etmeli")
            void getDeletedFiles_delegates() {
                when(fileItemDao.findDeletedByOwner(testUser)).thenReturn(List.of(testFile));

                List<FileItem> result = fileService.getDeletedFiles(testUser);

                assertEquals(1, result.size());
            }

            @Test
            @DisplayName("getStarredFiles — DAO'ya delege etmeli")
            void getStarredFiles_delegates() {
                when(fileItemDao.findStarredByOwner(testUser)).thenReturn(List.of(testFile));

                List<FileItem> result = fileService.getStarredFiles(testUser);

                assertEquals(1, result.size());
            }

            @Test
            @DisplayName("getRecentFiles — DAO'ya delege etmeli")
            void getRecentFiles_delegates() {
                when(fileItemDao.findRecentByOwner(testUser, 5)).thenReturn(List.of(testFile));

                List<FileItem> result = fileService.getRecentFiles(testUser);

                assertEquals(1, result.size());
            }

            @Test
            @DisplayName("getFilesInTrash — DAO'ya delege etmeli")
            void getFilesInTrash_delegates() {
                when(fileItemDao.findByFolderAndOwnerInTrash(testFolder, testUser)).thenReturn(
                        List.of(testFile));

                List<FileItem> result = fileService.getFilesInTrash(testFolder, testUser);

                assertEquals(1, result.size());
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // Yardımcılar
        // ═══════════════════════════════════════════════════════════════════════════

        /**
         * Sadece getSize() stub'lar. getContentType/getInputStream sadece başarı
         * akışlarında gerektiğinden ilgili test içinde ayrıca stub'lanmalı.
         * Hata fırlatan testler bu metodlara ulaşmadan döner; burada stub'lamak
         * UnnecessaryStubbingException'a yol açar.
         */
        private MultipartFile mockMultipart(String name, long size) {
            MultipartFile f = mock(MultipartFile.class);
            when(f.getSize()).thenReturn(size);
            lenient().when(f.getOriginalFilename()).thenReturn(name);
            return f;
        }

        private Folder folder(long id, User owner, boolean deleted) {
            Folder folder = new Folder();
            folder.setId(id);
            folder.setOwner(owner);
            folder.setDeleted(deleted);
            return folder;
        }
    }
}