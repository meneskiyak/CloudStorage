package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

@Service
@Transactional
public class AdminService {

    @Autowired
    private UserDao userDao;

    // Adminin bir kullanıcının Upload Kotasını (Örn: 5 GB) artırma/azaltma işlemi
    public void updateUserUploadLimit(String email, Long newLimit) throws Exception {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new Exception("Kullanıcı bulunamadı!");
        }
        user.setUploadLimitBytes(newLimit);

        userDao.save(user);
    }

}