package tk.project.bankcards.exception;

public class BankCardConflictException extends RuntimeException {
  public BankCardConflictException(String message) {
    super(message);
  }
}
