package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;

/**
 * Kullanıcı zaten mevcut olduğunda fırlatılan istisna.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
