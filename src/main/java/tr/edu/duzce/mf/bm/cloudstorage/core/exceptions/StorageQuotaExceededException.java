package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;


public class StorageQuotaExceededException extends RuntimeException {
    public StorageQuotaExceededException(String message) {
        super(message);
    }
}
