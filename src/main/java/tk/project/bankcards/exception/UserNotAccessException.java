package tk.project.bankcards.exception;

public class UserNotAccessException extends RuntimeException {
  public UserNotAccessException(String message) {
    super(message);
  }
}
