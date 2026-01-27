package tk.project.bankcards.config;

import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tk.project.bankcards.dto.ErrorResponse;
import tk.project.bankcards.exception.BankCardConflictException;
import tk.project.bankcards.exception.BankCardNotFoundException;
import tk.project.bankcards.exception.BankCardStatusNotActiveException;
import tk.project.bankcards.exception.NotEnoughBankCardBalanceException;
import tk.project.bankcards.exception.UserConflictException;
import tk.project.bankcards.exception.UserNotAccessException;
import tk.project.bankcards.exception.UserNotFoundException;

@Slf4j
@RestControllerAdvice
public class AppExceptionHandler {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({BankCardNotFoundException.class, UserNotFoundException.class})
  public ErrorResponse handleNotFound(RuntimeException ex) {
    return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler({BankCardConflictException.class, UserConflictException.class})
  public ErrorResponse handleConflict(RuntimeException ex) {
    return buildErrorResponse(ex, HttpStatus.CONFLICT);
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler({NotEnoughBankCardBalanceException.class, UserNotAccessException.class})
  public ErrorResponse handleForbidden(RuntimeException ex) {
    return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BankCardStatusNotActiveException.class)
  public ErrorResponse handleBadRequest(RuntimeException ex) {
    return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    String message =
        ex.getFieldErrors().stream()
            .map(
                fieldError ->
                    String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage()))
            .collect(Collectors.joining("; "));

    return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(Throwable.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleUncaughtThrowable(Throwable ex) {
    return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ErrorResponse buildErrorResponse(Throwable ex, HttpStatus status) {
    return buildErrorResponse(ex, status, ex.getMessage());
  }

  private ErrorResponse buildErrorResponse(Throwable ex, HttpStatus status, String message) {
    log.error("Ошибка [{}]: {}", status.value(), message, ex);
    return new ErrorResponse(ex.getClass().getSimpleName(), Instant.now(), message);
  }
}
