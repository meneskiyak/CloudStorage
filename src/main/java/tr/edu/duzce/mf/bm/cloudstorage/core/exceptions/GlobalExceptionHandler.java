package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Uygulama genelindeki istisnaları merkezi olarak yöneten sınıf.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Kaynak bulunamadığında (404) fırlatılan istisnaları yakalar.
     *
     * @param e Yakalanan istisna
     * @return 404 hata sayfası
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class, FileNotFoundException.class, FolderNotFoundException.class})
    public String handleNotFound(Exception e) {
        logger.warn("Kaynak bulunamadı: {}", e.getMessage());
        return "error/404";
    }

    /**
     * Yetkisiz erişim (403) durumunda fırlatılan istisnaları yakalar.
     *
     * @param e Yakalanan istisna
     * @return 403 hata sayfası
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e) {
        logger.warn("Yetkisiz erişim: {}", e.getMessage());
        return "error/403";
    }

    /**
     * Kullanıcı zaten mevcut olduğunda fırlatılan istisnayı yakalar.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public String handleUserAlreadyExists(UserAlreadyExistsException e) {
        logger.warn("Kayıt hatası: {}", e.getMessage());
        return "redirect:/register?error=exists";
    }

    /**
     * Geçersiz şifre veya kullanıcı bulunamadığında fırlatılan istisnaları yakalar.
     */
    @ExceptionHandler({InvalidPasswordException.class, UserNotFoundException.class})
    public String handleAuthExceptions(Exception e) {
        logger.warn("Kimlik doğrulama hatası: {}", e.getMessage());
        return "redirect:/login?error=invalid";
    }

    /**
     * Klasör zaten mevcut veya geçersiz argüman durumlarında fırlatılan istisnaları yakalar.
     */
    @ExceptionHandler({FolderAlreadyExistsException.class, IllegalArgumentException.class})
    public String handleInvalidInput(Exception e) {
        logger.warn("Geçersiz işlem isteği: {}", e.getMessage());
        return "redirect:/dashboard?error=invalid";
    }

    /**
     * Dosya boyutu sınırını aşan yükleme denemelerini yakalar.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeExceeded(MaxUploadSizeExceededException e) {
        logger.warn("Dosya boyutu sınırı aşıldı: {}", e.getMessage());
        return "redirect:/dashboard?error=maxSize";
    }

    /**
     * Depolama kotası aşıldığında fırlatılan istisnayı yakalar.
     *
     * @param e Yakalanan istisna
     * @return Dashboard sayfasına yönlendirme
     */
    @ExceptionHandler(StorageQuotaExceededException.class)
    public String handleQuotaExceeded(StorageQuotaExceededException e) {
        logger.info("Kota doldu uyarısı verildi.");
        return "redirect:/dashboard?error=quota";
    }

    /**
     * Depolama servisi hatalarını yakalar.
     */
    @ExceptionHandler(StorageException.class)
    public String handleStorageException(StorageException e) {
        logger.error("Depolama hatası: ", e);
        return "error/500";
    }

    /**
     * Yukarıdakiler dışındaki tüm genel istisnaları (500) yakalar.
     *
     * @param e Yakalanan istisna
     * @return 500 hata sayfası
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e) {
        logger.error("Kritik Sunucu Hatası: ", e);
        return "error/500";
    }
}
