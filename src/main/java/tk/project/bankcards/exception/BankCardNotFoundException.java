package tk.project.bankcards.exception;

public class BankCardNotFoundException extends RuntimeException {
  public BankCardNotFoundException(String message) {
    super(message);
  }
}
