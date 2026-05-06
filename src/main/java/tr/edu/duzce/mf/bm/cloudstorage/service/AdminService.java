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
    public void updateUserUploadLimit(String userEmail, Long newLimitBytes) throws Exception {
        User user = userDao.findByEmail(userEmail);

        if (user == null) {
            throw new Exception("Kullanıcı bulunamadı!");
        }

        // Eğer yeni limit, kullanıcının halihazırda kullandığı alandan daha küçükse hata ver
        if (newLimitBytes < user.getUsedBytes()) {
            throw new Exception("Hata: Yeni limit, kullanıcının mevcut doluluğundan küçük olamaz!");
        }

        user.setUploadLimitBytes(newLimitBytes);
    }
}