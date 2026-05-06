package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private AdminService adminService;

    // --- ÖZELLİK: KULLANICI KOTASINI GÜNCELLEME ---
    @Test
    public void testUpdateUserUploadLimit_Basarili() throws Exception {
        // 1. Hazırlık
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setUploadLimitBytes(1000L); // Eski limit 1000

        // Veritabanından bu e-posta arandığında bizim mockUser'ı dön
        Mockito.when(userDao.findByEmail("test@test.com")).thenReturn(mockUser);

        // 2. Eylem: Limiti 5000 olarak güncelliyoruz
        adminService.updateUserUploadLimit("test@test.com", 5000L);

        // 3. Doğrulama
        Assertions.assertEquals(5000L, mockUser.getUploadLimitBytes()); // Limit gerçekten 5000 olmuş mu?
        Mockito.verify(userDao, Mockito.times(1)).save(mockUser); // DAO'ya kaydet emri gitmiş mi?
    }

    // --- ÖZELLİK: KULLANICI BULUNAMAMASI DURUMU ---
    @Test
    public void testUpdateUserUploadLimit_KullaniciYok_HataFirlatmali() {
        // Veritabanında kullanıcı yoksa null dön
        Mockito.when(userDao.findByEmail("olmayan@test.com")).thenReturn(null);

        // Exception fırlatmasını bekliyoruz
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            adminService.updateUserUploadLimit("olmayan@test.com", 5000L);
        });

        Assertions.assertEquals("Kullanıcı bulunamadı!", exception.getMessage());
    }
}