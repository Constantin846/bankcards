package tk.project.bankcards.exception;

public class BankCardStatusNotActiveException extends RuntimeException {
  public BankCardStatusNotActiveException(String message) {
    super(message);
  }
}
