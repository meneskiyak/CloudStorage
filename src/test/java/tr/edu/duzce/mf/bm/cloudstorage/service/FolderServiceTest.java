package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FolderDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock
    private FolderDao folderDao;

    @InjectMocks
    private FolderService folderService;

    // --- ÖZELLİK 1: KLASÖR OLUŞTURMA ---
    @Test
    public void testCreateFolder() {
        Folder folder = new Folder();
        folder.setName("Gizli Dosyalar");

        folderService.createFolder(folder);

        // Doğrulama: DAO'nun saveOrUpdate metodu çağrıldı mı?
        Mockito.verify(folderDao, Mockito.times(1)).saveOrUpdate(folder);
    }

    // --- ÖZELLİK 2: ALT KLASÖRLERİ GETİRME ---
    @Test
    public void testGetSubFolders() {
        Folder parent = new Folder();
        User owner = new User();

        // DAO'dan sanki 2 tane alt klasör dönüyormuş gibi sahte (mock) liste hazırlıyoruz
        List<Folder> mockList = Arrays.asList(new Folder(), new Folder());
        Mockito.when(folderDao.findByParentAndOwner(parent, owner)).thenReturn(mockList);

        // Servisi çalıştırıyoruz
        List<Folder> result = folderService.getSubFolders(parent, owner);

        // Gelen listenin boyutu gerçekten bizim sahte listemizdeki gibi 2 mi?
        Assertions.assertEquals(2, result.size());
    }

    // --- ÖZELLİK 3: KLASÖR TAŞIMA ---
    @Test
    public void testMoveFolder() {
        Folder folderToMove = new Folder();
        folderToMove.setId(10L); // Taşınacak klasör

        Folder newParent = new Folder();
        newParent.setId(5L); // Hedef klasör

        // Veritabanından 10 id'li klasör istendiğinde bizimkini dön
        Mockito.when(folderDao.findById(10L)).thenReturn(folderToMove);

        // Taşıma işlemini tetikliyoruz
        folderService.moveFolder(10L, newParent);

        // Taşınan klasörün yeni "parent" (üst) klasörünün id'si 5 olmuş mu?
        Assertions.assertEquals(5L, folderToMove.getParent().getId());
    }

    @Test
    public void testDeleteFolder_Basarili() throws Exception {
        // 1. HAZIRLIK (Arrange)
        Folder mockFolder = new Folder();
        mockFolder.setId(1L);
        mockFolder.setName("Silinecek Klasör");

        // findById çağrıldığında sahte klasörü dön
        Mockito.when(folderDao.findById(1L)).thenReturn(mockFolder);

        // 2. EYLEM (Act)
        folderService.deleteFolder(1L);

        // 3. DOĞRULAMA (Assert)
        // DAO'nun delete metodu bu klasörle tam 1 kere çağrılmış mı?
        Mockito.verify(folderDao, Mockito.times(1)).delete(mockFolder);
    }

    @Test
    public void testDeleteFolder_KlasorYok_HataFirlatmali() {
        // findById çağrıldığında null dönsün (klasör yok)
        Mockito.when(folderDao.findById(99L)).thenReturn(null);

        // Exception fırlatıp fırlatmadığını kontrol et
        Assertions.assertThrows(Exception.class, () -> {
            folderService.deleteFolder(99L);
        });

        // Hata fırladığı için delete metodu HİÇ çağrılmamış olmalı
        Mockito.verify(folderDao, Mockito.times(0)).delete(Mockito.any());
    }
}