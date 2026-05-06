package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

@Service
@Transactional // Yol haritasındaki zorunlu ister
public class UserService {

    @Autowired
    private UserDao userDao;

    public void registerUser(User user) {
        // TODO: Bcrypt hashleme ve mail onay mantığı yazılacak.
        userDao.save(user);
    }

    public User login(String email, String password) {
        User user = userDao.findByEmail(email);

        // TODO: İleride burada password.equals() yerine BCrypt.checkpw() kullanılacak.
        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        }
        return null;
    }
}