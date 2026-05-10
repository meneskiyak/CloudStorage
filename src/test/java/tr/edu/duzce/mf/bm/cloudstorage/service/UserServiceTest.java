package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.InvalidPasswordException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.UserAlreadyExistsException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.UserNotFoundException;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FileItemDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private FileItemDao fileItemDao;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(BCrypt.hashpw("Password123!", BCrypt.gensalt()));
        testUser.setUploadLimitBytes(1024L * 1024L); // 1 MB
        testUser.setUsedBytes(0L);
    }

    @Test
    @DisplayName("Kullanıcı ID ile bulunmalı")
    void shouldFindUserById() {
        // ID ile kullanıcı başarıyla bulunmalı
        when(userDao.findById(1L)).thenReturn(testUser);

        User found = userService.findById(1L);

        assertNotNull(found);
        assertEquals(testUser.getEmail(), found.getEmail());
        verify(userDao).findById(1L);
    }

    @Test
    @DisplayName("Olmayan kullanıcı ID ile arandığında hata fırlatmalı")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Bulunmayan bir ID için UserNotFoundException fırlatmalı
        when(userDao.findById(2L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.findById(2L));
    }

    @Test
    @DisplayName("Yeni kullanıcı başarıyla kaydedilmeli")
    void shouldRegisterUserSuccessfully() {
        // Geçerli bilgilerle kullanıcı kaydı başarılı olmalı
        when(userDao.findByEmail(anyString())).thenReturn(null);

        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPasswordHash("Password123!");

        userService.registerUser(newUser);

        verify(userDao).save(newUser);
        assertTrue(BCrypt.checkpw("Password123!", newUser.getPasswordHash()));
    }

    @Test
    @DisplayName("Aynı e-posta ile kayıt olmaya çalışıldığında hata fırlatmalı")
    void shouldThrowExceptionWhenRegisteringDuplicateEmail() {
        // Mevcut bir e-posta ile kayıt engellenmeli
        when(userDao.findByEmail("test@example.com")).thenReturn(testUser);

        User duplicateUser = new User();
        duplicateUser.setEmail("test@example.com");

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(duplicateUser));
    }

    @Test
    @DisplayName("Geçersiz e-posta formatı hata fırlatmalı")
    void shouldThrowExceptionForInvalidEmailFormat() {
        // Yanlış formatta e-posta girildiğinde IllegalArgumentException fırlatmalı
        User invalidUser = new User();
        invalidUser.setEmail("invalid-email");

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUser));
    }

    @Test
    @DisplayName("Başarılı giriş işlemi")
    void shouldLoginSuccessfully() {
        // Doğru kimlik bilgileriyle giriş yapılabilmeli
        when(userDao.findByEmail("test@example.com")).thenReturn(testUser);

        User loggedIn = userService.login("test@example.com", "Password123!");

        assertNotNull(loggedIn);
        assertEquals(testUser.getEmail(), loggedIn.getEmail());
    }

    @Test
    @DisplayName("Hatalı şifre ile giriş denemesi")
    void shouldThrowExceptionForWrongPassword() {
        // Yanlış şifre girildiğinde InvalidPasswordException fırlatmalı
        when(userDao.findByEmail("test@example.com")).thenReturn(testUser);

        assertThrows(InvalidPasswordException.class, () -> userService.login("test@example.com", "wrongpassword"));
    }

    @Test
    @DisplayName("Kullanıcı bilgileri güncellenmeli")
    void shouldUpdateUser() {
        // Mevcut bir kullanıcının bilgileri güncellenebilmeli
        when(userDao.findById(1L)).thenReturn(testUser);

        testUser.setFullName("Updated Name");
        userService.updateUser(testUser);

        verify(userDao).update(testUser);
    }

    @Test
    @DisplayName("Sistem genelindeki istatistikler doğru hesaplanmalı")
    void shouldCalculateGlobalStats() {
        // Admin için global istatistikler doğru şekilde toplanmalı
        User user1 = new User();
        user1.setUploadLimitBytes(1000L);
        user1.setUsedBytes(400L);

        User user2 = new User();
        user2.setUploadLimitBytes(2000L);
        user2.setUsedBytes(600L);

        when(userDao.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(fileItemDao.countFilesByMimeTypeGroup()).thenReturn(new HashMap<>());
        when(fileItemDao.findGlobalRecentItems(5)).thenReturn(new ArrayList<>());

        Map<String, Object> stats = userService.getGlobalStats();

        assertEquals(3000L, stats.get("totalLimit"));
        assertEquals(1000L, stats.get("totalUsed"));
        assertEquals(2000L, stats.get("totalFree"));
        assertEquals(2, stats.get("userCount"));
    }

    @Test
    @DisplayName("Geçen süre i18n anahtarı olarak doğru dönmeli")
    void shouldReturnCorrectTimeAgoKey() {
        // Zaman farkına göre doğru i18n anahtarı dönmeli
        Calendar cal = Calendar.getInstance();
        
        // Şimdi
        assertEquals("time.now", userService.getTimeAgo(cal.getTime()));

        // 10 dakika önce
        cal.add(Calendar.MINUTE, -10);
        assertEquals("time.minutesAgo,10", userService.getTimeAgo(cal.getTime()));

        // 5 saat önce
        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -5);
        assertEquals("time.hoursAgo,5", userService.getTimeAgo(cal.getTime()));

        // 2 gün önce
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        assertEquals("time.daysAgo,2", userService.getTimeAgo(cal.getTime()));
    }
}
