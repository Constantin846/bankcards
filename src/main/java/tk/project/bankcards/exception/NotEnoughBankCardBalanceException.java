package tk.project.bankcards.exception;

public class NotEnoughBankCardBalanceException extends RuntimeException {
  public NotEnoughBankCardBalanceException(String message) {
    super(message);
  }
}
