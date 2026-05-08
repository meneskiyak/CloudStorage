package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;

public class FolderAlreadyExistsException extends RuntimeException {
    public FolderAlreadyExistsException(String message) {
        super(message);
    }
}
