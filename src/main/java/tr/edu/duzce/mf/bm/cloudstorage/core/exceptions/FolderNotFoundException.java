package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;

public class FolderNotFoundException extends RuntimeException {
    public FolderNotFoundException(String message) {
        super(message);
    }
}
