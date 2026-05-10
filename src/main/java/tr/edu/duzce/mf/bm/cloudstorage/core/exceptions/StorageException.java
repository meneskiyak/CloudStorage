package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;

/**
 * Depolama servisi (MinIO vb.) kaynaklı hatalar için fırlatılan özel istisna sınıfı.
 */
public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
