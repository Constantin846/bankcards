package tk.project.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tk.project.bankcards.dto.BankCardCreateDto;
import tk.project.bankcards.dto.BankCardInfoDto;
import tk.project.bankcards.dto.BankCardShortInfoDto;
import tk.project.bankcards.dto.TransferDto;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.service.BankCardService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${app.controller.base-path}" + "${app.controller.bank-cards-path}")
@Tag(name = "BankCardController", description = "API для работы с банковскими картами")
public class BankCardController {

  private final BankCardService bankCardService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("${app.controller.admin-access}")
  @Operation(summary = "Создание банковской карты")
  public BankCardInfoDto create(@Valid @RequestBody BankCardCreateDto bankCard) {
    log.info(
        "Получен запрос на создание банковской карты с номером {}, c id владельца {} и сроком действия {}.",
        bankCard.number(),
        bankCard.ownerId(),
        bankCard.expiryDate());

    BankCardInfoDto savedBankCard = bankCardService.create(bankCard);

    log.info(
        "Выполнен запрос на создание банковской карты с номером {}, c id владельца {} и сроком действия {}.",
        savedBankCard.number(),
        savedBankCard.owner().id(),
        savedBankCard.expiryDate());
    return savedBankCard;
  }

  @PatchMapping("${app.controller.admin-access}/block/{cardId}")
  @Operation(summary = "Блокировка банковской карты")
  public BankCardInfoDto block(@PathVariable("cardId") UUID cardId) {
    log.info("Получен запрос на блокировку банковской карты c id {}.", cardId);

    BankCardInfoDto updatedBankCard = bankCardService.blockCardById(cardId);

    log.info("Выполнен запрос на блокировку банковской карты c id {}.", cardId);
    return updatedBankCard;
  }

  @PatchMapping("${app.controller.admin-access}/activate/{cardId}")
  @Operation(summary = "Активация  банковской карты")
  public BankCardInfoDto activate(@PathVariable("cardId") UUID cardId) {
    log.info("Получен запрос на активацию банковской карты c id {}.", cardId);

    BankCardInfoDto updatedBankCard = bankCardService.activateCardById(cardId);

    log.info("Выполнен запрос на активацию банковской карты c id {}.", cardId);
    return updatedBankCard;
  }

  @GetMapping("${app.controller.admin-access}")
  @Operation(summary = "Получение информации о банковских картах")
  public List<BankCardShortInfoDto> findAll(
      @PageableDefault(size = 20, sort = "number", direction = Sort.Direction.ASC)
          Pageable pageable) {

    log.info("Получен запрос на получение информации о банковских картах.");

    List<BankCardShortInfoDto> bankCards = bankCardService.findAll(pageable);

    log.info("Выполнен запрос на получение информации о банковских картах.");
    return bankCards;
  }

  @DeleteMapping("${app.controller.admin-access}/{cardId}")
  @Operation(summary = "Удаление банковской карты")
  public BankCardInfoDto deleteById(@PathVariable("cardId") UUID cardId) {
    log.info("Получен запрос на удаление банковской карты c id {}.", cardId);

    BankCardInfoDto deletedBankCard = bankCardService.deleteById(cardId);

    log.info("Выполнен запрос на удаление банковской карты c id {}.", cardId);
    return deletedBankCard;
  }

  @GetMapping("${app.controller.user-access}")
  @Operation(summary = "Получение информации о банковских картах пользователя")
  public List<BankCardShortInfoDto> findAllByUserId(
      @AuthenticationPrincipal UserEntity authUser,
      @PageableDefault(size = 10, sort = "balance", direction = Sort.Direction.DESC)
          Pageable pageable) {

    log.info(
        "Получен запрос на получение информации о банковских счетах пользователя с id {}.",
        authUser.getId());

    List<BankCardShortInfoDto> bankCards =
        bankCardService.findAllByUserId(authUser.getId(), pageable);

    log.info(
        "Выполнен запрос на получение информации о банковских картах пользователя с id {}.",
        authUser.getId());
    return bankCards;
  }

  @GetMapping("${app.controller.user-access}/{cardId}")
  @Operation(summary = "Получение информации о банковской карте")
  public BankCardInfoDto getById(
      @PathVariable("cardId") UUID cardId, @AuthenticationPrincipal UserEntity authUser) {

    log.info("Получен запрос на получение информации о банковской карте с id {}.", cardId);

    BankCardInfoDto foundCard = bankCardService.getById(cardId, authUser);

    log.info("Выполнен запрос на получение информации о банковской карте: {}.", foundCard);
    return foundCard;
  }

  @PostMapping("${app.controller.user-access}/transfer-self")
  @Operation(summary = "Перевод средств между своими банковскими картами")
  public void transfer(
      @Valid @RequestBody TransferDto transferDto, @AuthenticationPrincipal UserEntity authUser) {

    log.info(
        "Получен запрос на перевод средств с банковской карты с id {} на карту с id {} в размере {}.",
        transferDto.fromCardId(),
        transferDto.toCardId(),
        transferDto.amount());

    bankCardService.transferBetweenOwnerCard(transferDto, authUser);

    log.info(
        "Выполнен запрос на перевод средств с банковской карты с id {} на карту с id {} в размере {}.",
        transferDto.fromCardId(),
        transferDto.toCardId(),
        transferDto.amount());
  }
}
