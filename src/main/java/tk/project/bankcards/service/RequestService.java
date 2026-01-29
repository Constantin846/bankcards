package tk.project.bankcards.service;

import static tk.project.bankcards.util.BankCardStatusChecker.checkBankCardIsActive;
import static tk.project.bankcards.util.OwnershipChecker.checkOwnership;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.project.bankcards.entity.BankCardEntity;
import tk.project.bankcards.entity.RequestEntity;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.enums.RequestAction;
import tk.project.bankcards.enums.RequestStatus;
import tk.project.bankcards.exception.BankCardNotFoundException;
import tk.project.bankcards.repository.BankCardRepository;
import tk.project.bankcards.repository.RequestRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {

  private final BankCardRepository bankCardRepository;
  private final RequestRepository requestRepository;

  public UUID createBlockCardRequest(UUID cardId, UserEntity authUser) {
    BankCardEntity existingCard = getCardById(cardId);
    checkOwnership(authUser, existingCard);
    checkBankCardIsActive(existingCard);

    RequestEntity savedRequest =
        RequestEntity.builder()
            .owner(authUser)
            .bankCardId(cardId)
            .action(RequestAction.BLOCK_BANK_CARD)
            .status(RequestStatus.PENDING)
            .build();
    requestRepository.save(savedRequest);

    log.debug(
        "Запрос на блокировку карты с id {} от пользователя с id {} сохранена с id: {}.",
        savedRequest.getBankCardId(),
        savedRequest.getOwner().getId(),
        savedRequest.getId());
    return savedRequest.getId();
  }

  private BankCardEntity getCardById(UUID cardId) {
    return bankCardRepository
        .findById(cardId)
        .orElseThrow(
            () ->
                new BankCardNotFoundException(
                    String.format("Банковская карта с id %s не найдена", cardId)));
  }
}
