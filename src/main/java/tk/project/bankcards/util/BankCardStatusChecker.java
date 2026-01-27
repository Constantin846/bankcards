package tk.project.bankcards.util;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import tk.project.bankcards.entity.BankCardEntity;
import tk.project.bankcards.enums.BankCardStatus;
import tk.project.bankcards.exception.BankCardStatusNotActiveException;

@UtilityClass
public class BankCardStatusChecker {

  public static void checkBankCardIsActive(BankCardEntity bankCard) {
    if (!Objects.equals(bankCard.getStatus(), BankCardStatus.ACTIVE)) {
      throw new BankCardStatusNotActiveException(
          String.format(
              "Банковская карта с id %s не активна, текущий статус: %s.",
              bankCard.getId(), bankCard.getStatus()));
    }
  }
}
