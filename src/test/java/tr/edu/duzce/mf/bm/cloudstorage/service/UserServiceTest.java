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
public class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    // --- EKSİĞİNİ YAKALADIĞIN ÖZELLİK: KULLANICI KAYDI ---
    @Test
    public void testRegisterUser() {
        User newUser = new User();
        newUser.setEmail("yeni@test.com");
        newUser.setPasswordHash("12345");

        // Servisi tetikliyoruz
        userService.registerUser(newUser);

        // Doğrulama: DAO'nun save metodu bu yeni kullanıcıyla tam 1 kere çağrılmış olmalı
        Mockito.verify(userDao, Mockito.times(1)).save(newUser);
    }

    // --- ÖZELLİK 2: BAŞARILI GİRİŞ ---
    @Test
    public void testLogin_BasariliGiris() {
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setPasswordHash("1234");

        Mockito.when(userDao.findByEmail("test@test.com")).thenReturn(mockUser);

        User result = userService.login("test@test.com", "1234");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("test@test.com", result.getEmail());
    }

    // --- ÖZELLİK 3: HATALI ŞİFRE İLE GİRİŞ ---
    @Test
    public void testLogin_HataliSifre() {
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setPasswordHash("1234"); // Sistemdeki doğru şifre

        Mockito.when(userDao.findByEmail("test@test.com")).thenReturn(mockUser);

        // Kullanıcı 9999 girerek girmeyi deniyor
        User result = userService.login("test@test.com", "9999");

        // Sonuç kesinlikle null dönmeli
        Assertions.assertNull(result);
    }
}