package tr.edu.duzce.mf.bm.cloudstorage.service;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.duzce.mf.bm.cloudstorage.dao.UserDao;
import tr.edu.duzce.mf.bm.cloudstorage.dao.FileItemDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.InvalidPasswordException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.UserAlreadyExistsException;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.UserNotFoundException;

import java.util.*;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9çğıöşüÇĞİÖŞÜ+_.-]+@(.+)$";
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|_<>\\/-]).{8,}$";

    @Autowired
    private UserDao userDao;

    @Autowired
    private FileItemDao fileItemDao;

    @Autowired
    private MinioService minioService;

    /**
     * Sistem genelindeki istatistikleri hesaplar.
     */
    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();

        List<User> users = userDao.findAll();
        long totalLimit = 0;
        long totalUsed = 0;
        int userCount = users.size();

        for (User u : users) {
            totalLimit += u.getUploadLimitBytes();
            totalUsed += u.getUsedBytes();
        }

        stats.put("totalLimit", totalLimit);
        stats.put("totalUsed", totalUsed);
        stats.put("totalFree", Math.max(0, totalLimit - totalUsed));
        stats.put("userCount", userCount);

        // 1. Dosya Türü Dağılımı
        stats.put("fileTypeCounts", fileItemDao.countFilesByMimeTypeGroup());

        // 2. Son Aktiviteler (Sistem Geneli)
        stats.put("recentActivities", fileItemDao.findGlobalRecentItems(5));

        // 3. Kullanım Trendi
        Map<String, Long> trend = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        Calendar cal = Calendar.getInstance();
        
        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -i);
            String day = sdf.format(cal.getTime());
            trend.put(day, 0L); 
        }
        stats.put("usageTrend", trend);

        return stats;
    }

    public List<User> findAllUsers() {
        return userDao.findAll();
    }

    public void updateUser(User user) {
        if (userDao.findById(user.getId()) == null) {
            throw new UserNotFoundException("Güncellenmek istenen kullanıcı bulunamadı!");
        }
        userDao.update(user);
        logger.info("Kullanıcı bilgileri güncellendi: {}", user.getEmail());
    }

    public User findById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new UserNotFoundException("Kullanıcı bulunamadı!");
        }
        return user;
    }

    public void registerUser(User user) {
        validateEmail(user.getEmail());
        if (userDao.findByEmail(user.getEmail()) != null) {
            throw new UserAlreadyExistsException("Bu e-posta adresi zaten bir hesaba bağlı!");
        }
        String hashedPassword = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        userDao.save(user);
    }

    private void validateEmail(String email) {
        if (email == null || !Pattern.compile(EMAIL_PATTERN).matcher(email).matches()) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı!");
        }
    }

    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public User login(String email, String password) {
        User user = findByEmail(email);
        if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new InvalidPasswordException("Hatalı email veya şifre!");
        }
        return user;
    }

    /**
     * Verilen tarihin şu andan ne kadar süre önce olduğunu i18n anahtarı olarak döner.
     */
    public String getTimeAgo(Date date) {
        if (date == null) return "time.now";
        long diff = new Date().getTime() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) return "time.now";
        if (minutes < 60) return "time.minutesAgo," + minutes;
        if (hours < 24) return "time.hoursAgo," + hours;
        return "time.daysAgo," + days;
    }
}
