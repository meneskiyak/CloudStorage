package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

@Service
@Transactional // Yol haritasındaki zorunlu ister
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    /**
     * Yeni bir kullanıcı kaydeder. Şifreyi BCrypt ile hashler.
     *
     * @param user Kaydedilecek kullanıcı nesnesi
     */
    public void registerUser(User user) {
        // Şifreyi güvenli bir şekilde hashle
        String hashedPassword = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        
        userDao.save(user);
        logger.info("Yeni kullanıcı BCrypt ile hashlenerek kaydedildi: {}", user.getEmail());
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

        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            logger.info("Kullanıcı girişi başarılı (BCrypt doğrulandı): {}", email);
            return user;
        }
        
        logger.warn("Hatalı giriş denemesi veya geçersiz şifre: {}", email);
        return null;
    }
}