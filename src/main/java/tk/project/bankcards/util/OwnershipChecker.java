package tk.project.bankcards.util;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import tk.project.bankcards.entity.BankCardEntity;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.exception.UserNotAccessException;

@UtilityClass
public class OwnershipChecker {

  public static void checkOwnership(UserEntity authUser, BankCardEntity bankCard) {
    if (!Objects.equals(authUser.getId(), bankCard.getOwner().getId())) {
      throw new UserNotAccessException(
          String.format(
              "У пользователя с id %s нет доступа к банковской карте с id %s.",
              authUser.getId(), bankCard.getId()));
    }
  }
}
