package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.edu.duzce.mf.bm.cloudstorage.core.enums.Role;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.AccessDeniedException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderAlreadyExistsException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderNotFoundException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.StorageQuotaExceededException;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FileItemDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FolderDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock private FolderDao folderDao;
    @Mock private FileItemDao fileItemDao;
    @Mock private UserDao userDao;
    @Mock private MinioService minioService;
    @Mock private EmbeddingService embeddingService;

    @InjectMocks
    private FolderService folderService;

    private User testUser;
    private User otherUser;
    private Folder rootFolder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setRole(Role.USER);
        testUser.setUsedBytes(0L);
        testUser.setUploadLimitBytes(10L * 1024 * 1024); // 10 MB

        otherUser = new User();
        otherUser.setId(2L);

        rootFolder = new Folder();
        rootFolder.setId(10L);
        rootFolder.setName("Root");
        rootFolder.setOwner(testUser);
        rootFolder.setDeleted(false);
        rootFolder.setStarred(false);
        rootFolder.setChildren(new ArrayList<>());
        rootFolder.setFiles(new ArrayList<>());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createFolder
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createFolder")
    class CreateFolder {

        @Test
        @DisplayName("Root'ta başarılı klasör oluşturma")
        void success_rootLevel() {
            Folder newFolder = folder("New Folder", testUser, null, false);
            when(folderDao.findByParentAndOwner(null, testUser)).thenReturn(Collections.emptyList());

            folderService.createFolder(newFolder);

            verify(folderDao).save(newFolder);
        }

        @Test
        @DisplayName("Alt klasör olarak başarılı oluşturma")
        void success_withParent() {
            Folder newFolder = folder("Child", testUser, rootFolder, false);
            when(folderDao.findByParentAndOwner(rootFolder, testUser)).thenReturn(Collections.emptyList());

            folderService.createFolder(newFolder);

            verify(folderDao).save(newFolder);
        }

        @Test
        @DisplayName("Aynı isimde klasör — FolderAlreadyExistsException")
        void duplicateName_throws() {
            Folder existing = folder("Existing", testUser, null, false);
            Folder newFolder = folder("existing", testUser, null, false); // case-insensitive
            when(folderDao.findByParentAndOwner(null, testUser)).thenReturn(List.of(existing));

            assertThrows(FolderAlreadyExistsException.class,
                    () -> folderService.createFolder(newFolder));
            verify(folderDao, never()).save(any());
        }

        @Test
        @DisplayName("Başkasının parent'ına klasör oluşturma — AccessDeniedException")
        void parentOwnedByOther_throws() {
            Folder parent = folder("Parent", otherUser, null, false);
            Folder newFolder = folder("Child", testUser, parent, false);

            assertThrows(AccessDeniedException.class,
                    () -> folderService.createFolder(newFolder));
            verify(folderDao, never()).save(any());
        }

        @Test
        @DisplayName("Silinmiş parent'a klasör oluşturma — FolderNotFoundException")
        void deletedParent_throws() {
            Folder parent = folder("Parent", testUser, null, true);
            Folder newFolder = folder("Child", testUser, parent, false);

            assertThrows(FolderNotFoundException.class,
                    () -> folderService.createFolder(newFolder));
            verify(folderDao, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getFolder
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getFolder")
    class GetFolder {

        @Test
        @DisplayName("Başarılı — klasör döndürülmeli")
        void success() {
            when(folderDao.findById(10L)).thenReturn(rootFolder);

            Folder result = folderService.getFolder(10L);

            assertEquals(rootFolder, result);
        }

        @Test
        @DisplayName("Klasör bulunamazsa FolderNotFoundException")
        void notFound_throws() {
            when(folderDao.findById(99L)).thenReturn(null);

            assertThrows(FolderNotFoundException.class, () -> folderService.getFolder(99L));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // moveFolder
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("moveFolder")
    class MoveFolder {

        @Test
        @DisplayName("Başarılı taşıma — parent güncellenmeli")
        void success() {
            Folder toMove = folder("ToMove", testUser, null, false);
            toMove.setId(20L);
            Folder target = folder("Target", testUser, null, false);
            target.setId(30L);

            when(folderDao.findById(20L)).thenReturn(toMove);

            folderService.moveFolder(20L, target, testUser);

            assertEquals(target, toMove.getParent());
        }

        @Test
        @DisplayName("Root'a taşıma (null parent) başarılı")
        void moveToRoot_success() {
            Folder toMove = folder("ToMove", testUser, rootFolder, false);
            toMove.setId(20L);

            when(folderDao.findById(20L)).thenReturn(toMove);

            folderService.moveFolder(20L, null, testUser);

            assertNull(toMove.getParent());
        }

        @Test
        @DisplayName("Klasör bulunamazsa FolderNotFoundException")
        void notFound_throws() {
            when(folderDao.findById(99L)).thenReturn(null);

            assertThrows(FolderNotFoundException.class,
                    () -> folderService.moveFolder(99L, rootFolder, testUser));
        }

        @Test
        @DisplayName("Başkasının klasörünü taşıma — AccessDeniedException")
        void othersFolder_throws() {
            Folder toMove = folder("ToMove", otherUser, null, false);
            toMove.setId(20L);
            when(folderDao.findById(20L)).thenReturn(toMove);

            assertThrows(AccessDeniedException.class,
                    () -> folderService.moveFolder(20L, rootFolder, testUser));
        }

        @Test
        @DisplayName("Hedef başkasının klasörü — AccessDeniedException")
        void targetOwnedByOther_throws() {
            Folder toMove = folder("ToMove", testUser, null, false);
            toMove.setId(20L);
            Folder target = folder("Target", otherUser, null, false);

            when(folderDao.findById(20L)).thenReturn(toMove);

            assertThrows(AccessDeniedException.class,
                    () -> folderService.moveFolder(20L, target, testUser));
        }

        @Test
        @DisplayName("Hedef silinmiş klasör — FolderNotFoundException")
        void targetDeleted_throws() {
            Folder toMove = folder("ToMove", testUser, null, false);
            toMove.setId(20L);
            Folder target = folder("Target", testUser, null, true);

            when(folderDao.findById(20L)).thenReturn(toMove);

            assertThrows(FolderNotFoundException.class,
                    () -> folderService.moveFolder(20L, target, testUser));
        }

        @Test
        @DisplayName("Kendi alt klasörüne taşıma — IllegalArgumentException (circular)")
        void circularReference_throws() {
            Folder parent = folder("Parent", testUser, null, false);
            parent.setId(20L);
            Folder child = folder("Child", testUser, null, false);
            child.setId(30L);
            child.setParent(parent); // child → parent zinciri

            when(folderDao.findById(20L)).thenReturn(parent);

            // parent'ı child'ın içine taşımak: child.parent = parent, yani circular
            assertThrows(IllegalArgumentException.class,
                    () -> folderService.moveFolder(20L, child, testUser));
        }

        @Test
        @DisplayName("Kendine taşıma — IllegalArgumentException")
        void moveToItself_throws() {
            Folder toMove = folder("ToMove", testUser, null, false);
            toMove.setId(20L);

            when(folderDao.findById(20L)).thenReturn(toMove);

            assertThrows(IllegalArgumentException.class,
                    () -> folderService.moveFolder(20L, toMove, testUser));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // softDeleteFolder
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("softDeleteFolder")
    class SoftDeleteFolder {

        @Test
        @DisplayName("Klasör, dosyalar ve alt klasörler silinmeli")
        void success_recursive() {
            Folder sub = folder("Sub", testUser, rootFolder, false);
            sub.setChildren(new ArrayList<>());
            FileItem file = fileItem(testUser);
            FileItem subFile = fileItem(testUser);
            sub.setFiles(List.of(subFile));
            rootFolder.getChildren().add(sub);
            rootFolder.getFiles().add(file);

            when(folderDao.findById(10L)).thenReturn(rootFolder);

            folderService.softDeleteFolder(10L, testUser);

            assertTrue(rootFolder.isDeleted());
            assertTrue(sub.isDeleted());
            assertTrue(file.isDeleted());
            assertTrue(subFile.isDeleted());
        }

        @Test
        @DisplayName("Klasör bulunamazsa FolderNotFoundException")
        void notFound_throws() {
            when(folderDao.findById(10L)).thenReturn(null);

            assertThrows(FolderNotFoundException.class,
                    () -> folderService.softDeleteFolder(10L, testUser));
        }

        @Test
        @DisplayName("Başkasının klasörü — AccessDeniedException")
        void othersFolder_throws() {
            rootFolder.setOwner(otherUser);
            when(folderDao.findById(10L)).thenReturn(rootFolder);

            assertThrows(AccessDeniedException.class,
                    () -> folderService.softDeleteFolder(10L, testUser));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // restoreFolder
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("restoreFolder")
    class RestoreFolder {

        @Test
        @DisplayName("Klasör, dosyalar ve alt klasörler geri yüklenmeli")
        void success_recursive() {
            rootFolder.setDeleted(true);
            Folder sub = folder("Sub", testUser, rootFolder, true);
            sub.setChildren(new ArrayList<>());
            FileItem file = fileItem(testUser);
            file.setDeleted(true);
            sub.setFiles(List.of(file));
            rootFolder.getChildren().add(sub);
            rootFolder.setFiles(new ArrayList<>());

            when(folderDao.findById(10L)).thenReturn(rootFolder);

            folderService.restoreFolder(10L, testUser);

            assertFalse(rootFolder.isDeleted());
            assertFalse(sub.isDeleted());
            assertFalse(file.isDeleted());
        }

        @Test
        @DisplayName("Klasör bulunamazsa FolderNotFoundException")
        void notFound_throws() {
            when(folderDao.findById(10L)).thenReturn(null);

            assertThrows(FolderNotFoundException.class,
                    () -> folderService.restoreFolder(10L, testUser));
        }

        @Test
        @DisplayName("Başkasının klasörü — AccessDeniedException")
        void othersFolder_throws() {
            rootFolder.setOwner(otherUser);
            when(folderDao.findById(10L)).thenReturn(rootFolder);

            assertThrows(AccessDeniedException.class,
                    () -> folderService.restoreFolder(10L, testUser));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // permanentlyDeleteFolder (MinIO çağrıları pas geçildi)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("permanentlyDeleteFolder")
    class PermanentlyDeleteFolder {

        @Test
        @DisplayName("Başarılı kalıcı silme — DB ve kota güncellenmeli")
        void success() {
            testUser.setUsedBytes(1000L);
            FileItem file = fileItem(testUser);
            file.setFileSizeBytes(400L);
            file.setDeleted(true);
            rootFolder.setFiles(List.of(file));

            when(folderDao.findById(10L)).thenReturn(rootFolder);
            when(userDao.findById(1L)).thenReturn(testUser);

            folderService.permanentlyDeleteFolder(10L, testUser);

            verify(fileItemDao).delete(file);
            verify(folderDao).delete(rootFolder);
            assertEquals(1000L - 400L, testUser.getUsedBytes());
        }

        @Test
        @DisplayName("Kota sıfırın altına düşmemeli")
        void usedBytes_neverNegative() {
            testUser.setUsedBytes(0L);
            rootFolder.setFiles(new ArrayList<>());

            when(folderDao.findById(10L)).thenReturn(rootFolder);
            when(userDao.findById(1L)).thenReturn(testUser);

            folderService.permanentlyDeleteFolder(10L, testUser);

            assertEquals(0L, testUser.getUsedBytes());
        }

        @Test
        @DisplayName("Alt klasörler de kalıcı silinmeli (recursive)")
        void success_recursive() {
            Folder sub = folder("Sub", testUser, rootFolder, true);
            sub.setChildren(new ArrayList<>());
            FileItem subFile = fileItem(testUser);
            subFile.setDeleted(true);
            sub.setFiles(List.of(subFile));
            rootFolder.getChildren().add(sub);
            rootFolder.setFiles(new ArrayList<>());

            when(folderDao.findById(10L)).thenReturn(rootFolder);
            when(userDao.findById(1L)).thenReturn(testUser);

            folderService.permanentlyDeleteFolder(10L, testUser);

            verify(fileItemDao).delete(subFile);
            verify(folderDao).delete(sub);
            verify(folderDao).delete(rootFolder);
        }

        @Test
        @DisplayName("Klasör bulunamazsa FolderNotFoundException")
        void notFound_throws() {
            when(folderDao.findById(10L)).thenReturn(null);

            assertThrows(FolderNotFoundException.class,
                    () -> folderService.permanentlyDeleteFolder(10L, testUser));
        }

        @Test
        @DisplayName("Başkasının klasörü — AccessDeniedException")
        void othersFolder_throws() {
            rootFolder.setOwner(otherUser);
            when(folderDao.findById(10L)).thenReturn(rootFolder);

            assertThrows(AccessDeniedException.class,
                    () -> folderService.permanentlyDeleteFolder(10L, testUser));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // renameFolder
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("renameFolder")
    class RenameFolder {

        @Test
        @DisplayName("Başarılı yeniden adlandırma")
        void success() {
            when(folderDao.findById(10L)).thenReturn(rootFolder);
            when(folderDao.findByParentAndOwner(null, testUser)).thenReturn(Collections.emptyList());

            folderService.renameFolder(10L, "New Name", testUser);

            assertEquals("New Name", rootFolder.getName());
        }

        @Test
        @DisplayName("Aynı isimde başka klasör varsa FolderAlreadyExistsException")
        void duplicateName_throws() {
            Folder sibling = folder("Taken", testUser, null, false);
            sibling.setId(99L);
            when(folderDao.findById(10L)).thenReturn(rootFolder);
            when(folderDao.findByParentAndOwner(null, testUser)).thenReturn(List.of(sibling));

            assertThrows(FolderAlreadyExistsException.class,
                    () -> folderService.renameFolder(10L, "taken", testUser)); // case-insensitive
        }

        @Test
        @DisplayName("Aynı klasörün kendisiyle çakışması engellenmemeli")
        void sameFolder_notConflict() {
            // findByParentAndOwner klasörün kendisini de döndürebilir;
            // id eşleşmesi sayesinde çakışma sayılmamalı
            when(folderDao.findById(10L)).thenReturn(rootFolder);
            when(folderDao.findByParentAndOwner(null, testUser)).thenReturn(List.of(rootFolder));

            folderService.renameFolder(10L, "Root Renamed", testUser);

            assertEquals("Root Renamed", rootFolder.getName());
        }

        @Test
        @DisplayName("Klasör bulunamazsa FolderNotFoundException")
        void notFound_throws() {
            when(folderDao.findById(10L)).thenReturn(null);

            assertThrows(FolderNotFoundException.class,
                    () -> folderService.renameFolder(10L, "X", testUser));
        }

        @Test
        @DisplayName("Başkasının klasörü — AccessDeniedException")
        void othersFolder_throws() {
            rootFolder.setOwner(otherUser);
            when(folderDao.findById(10L)).thenReturn(rootFolder);

            assertThrows(AccessDeniedException.class,
                    () -> folderService.renameFolder(10L, "X", testUser));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // toggleStar
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toggleStar")
    class ToggleStar {

        @Test
        @DisplayName("Yıldız durumu toggle edilmeli")
        void togglesTwice() {
            when(folderDao.findById(10L)).thenReturn(rootFolder);
            assertFalse(rootFolder.isStarred());

            folderService.toggleStar(10L, testUser);
            assertTrue(rootFolder.isStarred());

            folderService.toggleStar(10L, testUser);
            assertFalse(rootFolder.isStarred());
        }

        @Test
        @DisplayName("Klasör bulunamazsa FolderNotFoundException")
        void notFound_throws() {
            when(folderDao.findById(10L)).thenReturn(null);

            assertThrows(FolderNotFoundException.class,
                    () -> folderService.toggleStar(10L, testUser));
        }

        @Test
        @DisplayName("Başkasının klasörü — AccessDeniedException")
        void othersFolder_throws() {
            rootFolder.setOwner(otherUser);
            when(folderDao.findById(10L)).thenReturn(rootFolder);

            assertThrows(AccessDeniedException.class,
                    () -> folderService.toggleStar(10L, testUser));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // touchFolder
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("touchFolder")
    class TouchFolder {

        @Test
        @DisplayName("Sahip — updatedAt ve update çağrılmalı")
        void success() {
            when(folderDao.findById(10L)).thenReturn(rootFolder);

            folderService.touchFolder(10L, testUser);

            assertNotNull(rootFolder.getUpdatedAt());
            verify(folderDao).update(rootFolder);
        }

        @Test
        @DisplayName("Klasör bulunamazsa hiçbir şey yapılmamalı")
        void notFound_doesNothing() {
            when(folderDao.findById(10L)).thenReturn(null);

            assertDoesNotThrow(() -> folderService.touchFolder(10L, testUser));
            verify(folderDao, never()).update(any());
        }

        @Test
        @DisplayName("Başkasının klasörü — hiçbir şey yapılmamalı")
        void othersFolder_doesNothing() {
            rootFolder.setOwner(otherUser);
            when(folderDao.findById(10L)).thenReturn(rootFolder);

            assertDoesNotThrow(() -> folderService.touchFolder(10L, testUser));
            verify(folderDao, never()).update(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getFolderPath
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getFolderPath")
    class GetFolderPath {

        @Test
        @DisplayName("Hiyerarşik path doğru sırada dönmeli")
        void success_returnsPathInOrder() {
            Folder grandParent = folder("GP", testUser, null, false);
            grandParent.setId(1L);
            Folder parent = folder("Parent", testUser, grandParent, false);
            parent.setId(2L);
            Folder child = folder("Child", testUser, parent, false);
            child.setId(3L);

            when(folderDao.findById(3L)).thenReturn(child);

            List<Folder> path = folderService.getFolderPath(3L);

            assertEquals(3, path.size());
            assertEquals(grandParent, path.get(0));
            assertEquals(parent, path.get(1));
            assertEquals(child, path.get(2));
        }

        @Test
        @DisplayName("Null id — boş liste dönmeli")
        void nullId_returnsEmpty() {
            List<Folder> path = folderService.getFolderPath(null);

            assertTrue(path.isEmpty());
        }

        @Test
        @DisplayName("Klasör bulunamazsa boş liste dönmeli")
        void notFound_returnsEmpty() {
            when(folderDao.findById(99L)).thenReturn(null);

            List<Folder> path = folderService.getFolderPath(99L);

            assertTrue(path.isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // uploadFolder — MinIO pas geçildi, iş mantığı kontrolleri test edildi
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("uploadFolder — iş mantığı kontrolleri")
    class UploadFolder {

        @Test
        @DisplayName("Kota aşıldığında StorageQuotaExceededException")
        void quotaExceeded_throws() {
            testUser.setUploadLimitBytes(100L);
            testUser.setUsedBytes(50L);

            org.springframework.mock.web.MockMultipartFile f =
                    new org.springframework.mock.web.MockMultipartFile(
                            "file", "a.txt", "text/plain", new byte[200]);

            assertThrows(StorageQuotaExceededException.class,
                    () -> folderService.uploadFolder(List.of(f), List.of("a.txt"), null, testUser));
        }

        @Test
        @DisplayName("Parent başkasına ait — AccessDeniedException")
        void parentOwnedByOther_throws() {
            rootFolder.setOwner(otherUser);
            org.springframework.mock.web.MockMultipartFile f =
                    new org.springframework.mock.web.MockMultipartFile(
                            "file", "a.txt", "text/plain", new byte[10]);

            assertThrows(AccessDeniedException.class,
                    () -> folderService.uploadFolder(List.of(f), List.of("a.txt"), rootFolder, testUser));
        }

        @Test
        @DisplayName("Parent silinmiş — FolderNotFoundException")
        void parentDeleted_throws() {
            rootFolder.setDeleted(true);
            org.springframework.mock.web.MockMultipartFile f =
                    new org.springframework.mock.web.MockMultipartFile(
                            "file", "a.txt", "text/plain", new byte[10]);

            assertThrows(FolderNotFoundException.class,
                    () -> folderService.uploadFolder(List.of(f), List.of("a.txt"), rootFolder, testUser));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Query metodları
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Query metodları")
    class QueryMethods {

        @Test
        @DisplayName("getSubFolders — DAO'ya delege etmeli")
        void getSubFolders_delegates() {
            when(folderDao.findByParentAndOwner(rootFolder, testUser)).thenReturn(List.of());

            folderService.getSubFolders(rootFolder, testUser);

            verify(folderDao).findByParentAndOwner(rootFolder, testUser);
        }

        @Test
        @DisplayName("getDeletedFolders — DAO'ya delege etmeli")
        void getDeletedFolders_delegates() {
            when(folderDao.findDeletedByOwner(testUser)).thenReturn(List.of(rootFolder));

            List<Folder> result = folderService.getDeletedFolders(testUser);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("getStarredFolders — DAO'ya delege etmeli")
        void getStarredFolders_delegates() {
            when(folderDao.findStarredByOwner(testUser)).thenReturn(List.of(rootFolder));

            List<Folder> result = folderService.getStarredFolders(testUser);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("searchFolders — DAO'ya delege etmeli")
        void searchFolders_delegates() {
            when(folderDao.searchByName("root", testUser)).thenReturn(List.of(rootFolder));

            List<Folder> result = folderService.searchFolders(testUser, "root");

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("getSubFoldersInTrash — DAO'ya delege etmeli")
        void getSubFoldersInTrash_delegates() {
            when(folderDao.findByParentAndOwnerInTrash(rootFolder, testUser)).thenReturn(List.of());

            folderService.getSubFoldersInTrash(rootFolder, testUser);

            verify(folderDao).findByParentAndOwnerInTrash(rootFolder, testUser);
        }

        @Test
        @DisplayName("getRecentFolders — DAO'ya delege etmeli")
        void getRecentFolders_delegates() {
            when(folderDao.findRecentByOwner(testUser, 5)).thenReturn(List.of(rootFolder));

            List<Folder> result = folderService.getRecentFolders(testUser);

            assertEquals(1, result.size());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Yardımcılar
    // ═══════════════════════════════════════════════════════════════════════════

    private Folder folder(String name, User owner, Folder parent, boolean deleted) {
        Folder f = new Folder();
        f.setName(name);
        f.setOwner(owner);
        f.setParent(parent);
        f.setDeleted(deleted);
        f.setChildren(new ArrayList<>());
        f.setFiles(new ArrayList<>());
        return f;
    }

    private FileItem fileItem(User owner) {
        FileItem f = new FileItem();
        f.setOwner(owner);
        f.setFileSizeBytes(512L);
        f.setStoredName("uuid_file.txt");
        f.setDeleted(false);
        return f;
    }
}