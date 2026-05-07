package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;

/**
 * Şifre belirlenen güvenlik kriterlerine uymadığında fırlatılan istisna.
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
