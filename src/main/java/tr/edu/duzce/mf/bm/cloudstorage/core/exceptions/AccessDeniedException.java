package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
