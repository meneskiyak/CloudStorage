package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.AccessDeniedException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderAlreadyExistsException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock
    private FolderDao folderDao;

    @Mock
    private FileItemDao fileItemDao;

    @Mock
    private UserDao userDao;

    @Mock
    private MinioService minioService;

    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private FolderService folderService;

    private User testUser;
    private Folder rootFolder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");

        rootFolder = new Folder();
        rootFolder.setId(10L);
        rootFolder.setName("Root");
        rootFolder.setOwner(testUser);
        rootFolder.setChildren(new ArrayList<>());
        rootFolder.setFiles(new ArrayList<>());
    }

    @Test
    @DisplayName("Başarılı klasör oluşturma")
    void shouldCreateFolderSuccessfully() {
        // Yeni bir klasör başarıyla kaydedilmeli
        Folder newFolder = new Folder();
        newFolder.setName("New Folder");
        newFolder.setOwner(testUser);

        when(folderDao.findByParentAndOwner(null, testUser)).thenReturn(Collections.emptyList());

        folderService.createFolder(newFolder);

        verify(folderDao).save(newFolder);
    }

    @Test
    @DisplayName("Aynı isimde klasör oluşturma engellenmeli")
    void shouldThrowExceptionWhenFolderAlreadyExists() {
        // Aynı dizinde aynı isimli klasör varsa hata fırlatmalı
        Folder existing = new Folder();
        existing.setName("Existing");
        
        Folder newFolder = new Folder();
        newFolder.setName("Existing");
        newFolder.setOwner(testUser);

        when(folderDao.findByParentAndOwner(null, testUser)).thenReturn(Collections.singletonList(existing));

        assertThrows(FolderAlreadyExistsException.class, () -> folderService.createFolder(newFolder));
    }

    @Test
    @DisplayName("Klasör başarıyla taşınmalı")
    void shouldMoveFolderSuccessfully() {
        // Klasörün üst dizini (parent) güncellenebilmeli
        Folder folderToMove = new Folder();
        folderToMove.setId(20L);
        folderToMove.setOwner(testUser);

        Folder targetParent = new Folder();
        targetParent.setId(30L);
        targetParent.setOwner(testUser);

        when(folderDao.findById(20L)).thenReturn(folderToMove);

        folderService.moveFolder(20L, targetParent, testUser);

        assertEquals(targetParent, folderToMove.getParent());
    }

    @Test
    @DisplayName("Klasör kendi alt klasörüne taşınamamalı (Circular Reference)")
    void shouldThrowExceptionForCircularMove() {
        // Sonsuz döngü oluşturacak taşımalar engellenmeli
        Folder parent = new Folder();
        parent.setId(20L);
        parent.setOwner(testUser);

        Folder child = new Folder();
        child.setId(30L);
        child.setParent(parent);
        child.setOwner(testUser);

        when(folderDao.findById(20L)).thenReturn(parent);

        assertThrows(IllegalArgumentException.class, () -> folderService.moveFolder(20L, child, testUser));
    }

    @Test
    @DisplayName("Klasör hiyerarşik olarak silinmeli (Soft Delete)")
    void shouldSoftDeleteFolderRecursively() {
        // Klasör silindiğinde içindeki tüm dosya ve alt klasörler de silindi işaretlenmeli
        Folder subFolder = new Folder();
        subFolder.setOwner(testUser);
        subFolder.setFiles(new ArrayList<>());
        subFolder.setChildren(new ArrayList<>());
        
        FileItem file = new FileItem();
        file.setOwner(testUser);
        
        rootFolder.getChildren().add(subFolder);
        rootFolder.getFiles().add(file);

        when(folderDao.findById(10L)).thenReturn(rootFolder);

        folderService.softDeleteFolder(10L, testUser);

        assertTrue(rootFolder.isDeleted());
        assertTrue(subFolder.isDeleted());
        assertTrue(file.isDeleted());
    }

    @Test
    @DisplayName("Klasör ismi güncellenmeli")
    void shouldRenameFolder() {
        // Klasör ismi çakışma yoksa değiştirilebilmeli
        when(folderDao.findById(10L)).thenReturn(rootFolder);
        when(folderDao.findByParentAndOwner(null, testUser)).thenReturn(Collections.emptyList());

        folderService.renameFolder(10L, "Updated Name", testUser);

        assertEquals("Updated Name", rootFolder.getName());
    }

    @Test
    @DisplayName("Başkasına ait klasörde işlem yapılması engellenmeli")
    void shouldThrowExceptionWhenAccessDenied() {
        // Başkasının klasörünü silmeye çalışan kullanıcı engellenmeli
        User otherUser = new User();
        otherUser.setId(2L);
        rootFolder.setOwner(otherUser);

        when(folderDao.findById(10L)).thenReturn(rootFolder);

        assertThrows(AccessDeniedException.class, () -> folderService.softDeleteFolder(10L, testUser));
    }
}
