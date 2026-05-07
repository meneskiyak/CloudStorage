package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
