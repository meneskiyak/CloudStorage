package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.InvalidPasswordException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.UserAlreadyExistsException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.UserNotFoundException;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional // Yol haritasındaki zorunlu ister
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    // E-posta regex deseni (Türkçe karakter desteği eklendi)
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9çğıöşüÇĞİÖŞÜ+_.-]+@(.+)$";
    
    // Şifre politikası: En az 8 karakter, 1 büyük, 1 küçük, 1 rakam, 1 özel karakter (Alt çizgi dahil)
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|_<>\\/-]).{8,}$";

    @Autowired
    private UserDao userDao;

    /**
     * Sistem genelindeki istatistikleri hesaplar.
     * @return Toplam kota, kullanılan alan ve kullanıcı sayısı bilgilerini içeren Map.
     */
    public java.util.Map<String, Object> getGlobalStats() {
        List<User> users = userDao.findAll();
        long totalLimit = 0;
        long totalUsed = 0;
        int userCount = users.size();

        for (User u : users) {
            totalLimit += u.getUploadLimitBytes();
            totalUsed += u.getUsedBytes();
        }

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalLimit", totalLimit);
        stats.put("totalUsed", totalUsed);
        stats.put("totalFree", Math.max(0, totalLimit - totalUsed));
        stats.put("userCount", userCount);
        return stats;
    }

    /**
     * Tüm kullanıcıları listeler (Admin paneli için).
     */
    public List<User> findAllUsers() {
        return userDao.findAll();
    }

    /**
     * Kullanıcıyı günceller (Rol veya kota değişikliği için).
     */
    public void updateUser(User user) {
        if (userDao.findById(user.getId()) == null) {
            throw new UserNotFoundException("Güncellenmek istenen kullanıcı bulunamadı!");
        }
        userDao.update(user);
        logger.info("Kullanıcı bilgileri güncellendi: {}", user.getEmail());
    }

    /**
     * ID üzerinden kullanıcı bulur.
     */
    public User findById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new UserNotFoundException("Kullanıcı bulunamadı!");
        }
        return user;
    }

    /**
     * Yeni bir kullanıcı kaydeder. E-posta ve şifre kriterlerini kontrol eder.
     * Şifreyi BCrypt ile hashler.
     *
     * @param user Kaydedilecek kullanıcı nesnesi
     */
    public void registerUser(User user) {
        validateEmail(user.getEmail());
        validatePassword(user.getPasswordHash()); // passwordHash alanında henüz ham şifre var

        // E-posta adresi kullanımda mı kontrol et
        if (userDao.findByEmail(user.getEmail()) != null) {
            logger.warn("Kayıt başarısız: E-posta adresi zaten kullanımda: {}", user.getEmail());
            throw new UserAlreadyExistsException("Bu e-posta adresi zaten bir hesaba bağlı!");
        }

        // Şifreyi güvenli bir şekilde hashle
        String hashedPassword = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        
        userDao.save(user);
        logger.info("Yeni kullanıcı başarıyla kaydedildi: {}", user.getEmail());
    }

    /**
     * Şifre güvenliğini sektör standartlarına göre doğrular.
     * 
     * @param password Doğrulanacak ham şifre
     */
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidPasswordException("Şifre boş olamaz!");
        }

        if (password.length() < 8) {
            throw new InvalidPasswordException("Şifre en az 8 karakter uzunluğunda olmalıdır!");
        }

        if (!Pattern.compile(PASSWORD_PATTERN).matcher(password).matches()) {
            throw new InvalidPasswordException("Şifre en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir!");
        }
    }

    /**
     * E-posta formatını ve uzunluğunu doğrular.
     * 
     * @param email Doğrulanacak e-posta
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-posta adresi boş olamaz!");
        }
        
        if (email.length() < 5 || email.length() > 100) {
            throw new IllegalArgumentException("E-posta adresi 5 ile 100 karakter arasında olmalıdır!");
        }

        if (!Pattern.compile(EMAIL_PATTERN).matcher(email).matches()) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı!");
        }
    }

    /**
     * Verilen e-posta adresine sahip kullanıcıyı bulur.
     *
     * @param email Aranacak e-posta adresi
     * @return Kullanıcı nesnesi veya null
     */
    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    /**
     * Kullanıcı girişi işlemini doğrular. Şifre kontrolü BCrypt ile yapılır.
     *
     * @param email    Kullanıcı e-postası
     * @param password Girilen ham şifre
     * @return Doğrulama başarılı ise User nesnesi, aksi halde null
     */
    public User login(String email, String password) {
        User user = userDao.findByEmail(email);

        if (user == null) {
            logger.warn("Giriş denemesi başarısız: Kullanıcı bulunamadı: {}", email);
            throw new UserNotFoundException("Bu e-posta adresine kayıtlı bir kullanıcı bulunamadı!");
        }

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            logger.warn("Giriş denemesi başarısız: Hatalı şifre: {}", email);
            throw new InvalidPasswordException("Girdiğiniz şifre hatalı!");
        }
        
        logger.info("Kullanıcı girişi başarılı (BCrypt doğrulandı): {}", email);
        return user;
    }
}