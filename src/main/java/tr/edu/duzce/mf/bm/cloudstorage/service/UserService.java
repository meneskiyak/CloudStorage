package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

@Service
@Transactional // Bu sınıftaki tüm veritabanı işlemleri Spring tarafından güvenle yönetilecek
public class UserService {

    @Autowired
    private UserDao userDao;

    // Yeni Kullanıcı Kaydı
    public void registerUser(User user) {

        // HASHLEME EKLENECEK!!!!
        userDao.save(user);
    }

    // Kullanıcı Girişi (Login) Kontrolü
    public User login(String email, String password) {
        User user = userDao.findByEmail(email);

        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        }

        // Eşleşme yoksa (hatalı şifre veya email) null döndür
        return null;
    }
}