package tk.project.bankcards.service;

import static tk.project.bankcards.util.BankCardStatusChecker.checkBankCardIsActive;
import static tk.project.bankcards.util.OwnershipChecker.checkOwnership;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.project.bankcards.dto.BankCardCreateDto;
import tk.project.bankcards.dto.BankCardInfoDto;
import tk.project.bankcards.dto.BankCardShortInfoDto;
import tk.project.bankcards.dto.TransferDto;
import tk.project.bankcards.entity.BankCardEntity;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.enums.BankCardStatus;
import tk.project.bankcards.exception.BankCardConflictException;
import tk.project.bankcards.exception.BankCardNotFoundException;
import tk.project.bankcards.exception.NotEnoughBankCardBalanceException;
import tk.project.bankcards.exception.UserNotFoundException;
import tk.project.bankcards.mapper.BankCardMapper;
import tk.project.bankcards.repository.BankCardRepository;
import tk.project.bankcards.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankCardService {

  private final BankCardMapper bankCardMapper;
  private final BankCardRepository bankCardRepository;
  private final UserRepository userRepository;

  public BankCardInfoDto create(BankCardCreateDto newCard) {
    if (bankCardRepository.findByNumber(newCard.number()).isPresent()) {
      throw new BankCardConflictException(
          String.format("Банковская карта c номером %s уже существует.", newCard.number()));
    }

    BankCardEntity bankCardEntity = bankCardMapper.toBankCardEntity(newCard);
    bankCardEntity.setOwner(getUserById(newCard.ownerId()));
    bankCardEntity.setStatus(BankCardStatus.ACTIVE);
    bankCardRepository.save(bankCardEntity);

    log.debug(
        "Банковская карта c номером {} и владельцем {} сохранена с id: {}.",
        bankCardEntity.getNumber(),
        bankCardEntity.getOwner().getId(),
        bankCardEntity.getId());
    return bankCardMapper.toBankCardInfoDto(bankCardEntity);
  }

  @Transactional
  public BankCardInfoDto blockCardById(UUID cardId) {
    BankCardEntity existingBankCard = getCardByIdForUpdate(cardId);
    checkBankCardIsActive(existingBankCard);

    existingBankCard.setStatus(BankCardStatus.BLOCKED);
    bankCardRepository.save(existingBankCard);

    log.debug(
        "Банковская карта с id: {}, c номером {} и владельцем {} заблокирована.",
        existingBankCard.getId(),
        existingBankCard.getNumber(),
        existingBankCard.getOwner().getId());
    return bankCardMapper.toBankCardInfoDto(existingBankCard);
  }

  @Transactional
  public BankCardInfoDto activateCardById(UUID cardId) {
    BankCardEntity existingBankCard = getCardByIdForUpdate(cardId);
    existingBankCard.setStatus(BankCardStatus.ACTIVE);
    bankCardRepository.save(existingBankCard);

    log.debug(
        "Банковская карта с id: {}, c номером {} и владельцем {} активирована.",
        existingBankCard.getId(),
        existingBankCard.getNumber(),
        existingBankCard.getOwner().getId());
    return bankCardMapper.toBankCardInfoDto(existingBankCard);
  }

  @Transactional
  public void transferBetweenOwnerCard(TransferDto transfer, UserEntity authUser) {
    BankCardEntity fromBankCard = getCardByIdForUpdate(transfer.fromCardId());
    checkOwnership(authUser, fromBankCard);
    checkBankCardIsActive(fromBankCard);

    if (fromBankCard.getBalance().compareTo(transfer.amount()) < 0) {
      throw new NotEnoughBankCardBalanceException(
          String.format(
              "На счету с id %s недостаточно средств для перевода.", fromBankCard.getId()));
    }

    BankCardEntity toBankCard = getCardByIdForUpdate(transfer.toCardId());
    checkOwnership(authUser, toBankCard);
    checkBankCardIsActive(toBankCard);

    fromBankCard.setBalance(fromBankCard.getBalance().subtract(transfer.amount()));
    toBankCard.setBalance(toBankCard.getBalance().add(transfer.amount()));

    bankCardRepository.save(fromBankCard);
    bankCardRepository.save(toBankCard);

    log.debug(
        "Перевод средств с банковской карты с id {}, на карту с id {} в размере {}.",
        fromBankCard.getId(),
        toBankCard.getId(),
        transfer.amount());
  }

  public List<BankCardShortInfoDto> findAll(Pageable pageable) {
    List<BankCardEntity> bankCards = bankCardRepository.findAll(pageable).stream().toList();
    log.debug("Найден список банковских карт.");
    return bankCardMapper.toBankCardShortInfoDto(bankCards);
  }

  public List<BankCardShortInfoDto> findAllByUserId(UUID userId, Pageable pageable) {
    List<BankCardEntity> bankCards = bankCardRepository.findAllByOwnerId(userId, pageable);
    log.debug("Найден список банковских карт пользователя с id {}.", userId);
    return bankCardMapper.toBankCardShortInfoDto(bankCards);
  }

  public BankCardInfoDto getById(UUID cardId, UserEntity authUser) {
    BankCardEntity foundBankCard = getCardById(cardId);
    checkOwnership(authUser, foundBankCard);

    log.debug(
        "Найдена банковская карта с id {}, c номером {} и владельцем {}.",
        foundBankCard.getId(),
        foundBankCard.getNumber(),
        foundBankCard.getOwner().getId());
    return bankCardMapper.toBankCardInfoDto(foundBankCard);
  }

  public BankCardInfoDto deleteById(UUID cardId) {
    BankCardEntity existingBankCard = getCardById(cardId);
    bankCardRepository.delete(existingBankCard);

    log.debug(
        "Банковская карта с id {}, c номером {} и владельцем {} удалена.",
        existingBankCard.getId(),
        existingBankCard.getNumber(),
        existingBankCard.getOwner().getId());
    return bankCardMapper.toBankCardInfoDto(existingBankCard);
  }

  private BankCardEntity getCardByIdForUpdate(UUID cardId) {
    return bankCardRepository
        .findByIdForUpdate(cardId)
        .orElseThrow(
            () ->
                new BankCardNotFoundException(
                    String.format("Банковская карта с id %s не найдена", cardId)));
  }

  private BankCardEntity getCardById(UUID cardId) {
    return bankCardRepository
        .findById(cardId)
        .orElseThrow(
            () ->
                new BankCardNotFoundException(
                    String.format("Банковская карта с id %s не найдена", cardId)));
  }

  private UserEntity getUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () ->
                new UserNotFoundException(String.format("Пользователь с id %s не найден", userId)));
  }
}
