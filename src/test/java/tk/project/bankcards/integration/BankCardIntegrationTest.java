package tk.project.bankcards.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import tk.project.bankcards.dto.BankCardCreateDto;
import tk.project.bankcards.dto.BankCardInfoDto;
import tk.project.bankcards.dto.ErrorResponse;
import tk.project.bankcards.dto.TransferDto;
import tk.project.bankcards.dto.UserInfoDto;
import tk.project.bankcards.entity.BankCardEntity;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.enums.BankCardStatus;
import tk.project.bankcards.enums.Role;
import tk.project.bankcards.exception.BankCardConflictException;
import tk.project.bankcards.exception.BankCardNotFoundException;
import tk.project.bankcards.exception.BankCardStatusNotActiveException;
import tk.project.bankcards.exception.NotEnoughBankCardBalanceException;
import tk.project.bankcards.exception.UserNotAccessException;
import tk.project.bankcards.exception.UserNotFoundException;

class BankCardIntegrationTest extends BaseIntegrationTest {

  @Test
  @SneakyThrows
  void createBankCard() {
    // GIVEN
    saveExistingUser();

    long number = 1234_1234_1234_1234L;
    String expectedNumber = "**** **** **** 1234";
    LocalDate expectedExpiryDate = LocalDate.now().plusMonths(1L);
    BankCardStatus expectedStatus = BankCardStatus.ACTIVE;
    BigDecimal expectedBalance = BigDecimal.valueOf(1454.345);

    BankCardCreateDto cardCreateDto =
        new BankCardCreateDto(number, existingUser.getId(), expectedExpiryDate, expectedBalance);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getAdminAccess())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(cardCreateDto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    BankCardInfoDto actualCard = objectMapper.readValue(result, BankCardInfoDto.class);

    // THEN
    assertNotNull(actualCard.id());
    assertEquals(expectedNumber, actualCard.number());
    assertUserEquals(existingUser, actualCard.owner());
    assertEquals(expectedExpiryDate, actualCard.expiryDate());
    assertEquals(expectedStatus, actualCard.status());
    assertEquals(expectedBalance, actualCard.balance());
  }

  @Test
  @SneakyThrows
  void createBankCardFailedIfCardNumberAlreadyExists() {
    // GIVEN
    saveExistingUser();

    long existingCardNumber = 1234_1234_1234_1234L;
    BankCardEntity existingCard =
        BankCardEntity.builder()
            .number(existingCardNumber)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.ONE)
            .build();
    bankCardRepository.save(existingCard);

    BankCardCreateDto cardCreateDto =
        new BankCardCreateDto(
            existingCardNumber,
            existingUser.getId(),
            LocalDate.now().plusMonths(1L),
            BigDecimal.valueOf(1454.345));

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getAdminAccess())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(cardCreateDto)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(BankCardConflictException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void createBankCardFailedIfUserNotFound() {
    // GIVEN
    BankCardCreateDto cardCreateDto =
        new BankCardCreateDto(
            1234_1234_1234_1234L,
            UUID.randomUUID(),
            LocalDate.now().plusMonths(1L),
            BigDecimal.valueOf(1454.345));

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getAdminAccess())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(cardCreateDto)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(UserNotFoundException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void blockBankCard() {
    // GIVEN
    saveExistingUser();

    long number = 1234_1234_1234_1234L;
    String expectedNumber = "**** **** **** 1234";
    LocalDate expectedExpiryDate = LocalDate.now().plusMonths(1L);
    BankCardStatus expectedStatus = BankCardStatus.BLOCKED;
    BigDecimal expectedBalance = BigDecimal.valueOf(1454.345);

    BankCardEntity existingCard =
        BankCardEntity.builder()
            .number(number)
            .owner(existingUser)
            .expiryDate(expectedExpiryDate)
            .status(BankCardStatus.ACTIVE)
            .balance(expectedBalance)
            .build();
    bankCardRepository.save(existingCard);

    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getBankCardsPath()
                            + endpointsConfig.getAdminAccess()
                            + "/block/"
                            + existingCard.getId())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    BankCardInfoDto actualCard = objectMapper.readValue(result, BankCardInfoDto.class);

    // THEN
    assertEquals(existingCard.getId(), actualCard.id());
    assertEquals(expectedNumber, actualCard.number());
    assertUserEquals(existingUser, actualCard.owner());
    assertEquals(expectedExpiryDate, actualCard.expiryDate());
    assertEquals(expectedStatus, actualCard.status());
    assertEquals(
        expectedBalance.setScale(4, RoundingMode.HALF_UP),
        actualCard.balance().setScale(4, RoundingMode.HALF_UP));
  }

  @Test
  @SneakyThrows
  void blockBankCardFailedIfBankCardNotActive() {
    // GIVEN
    saveExistingUser();

    BankCardEntity existingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(1L))
            .status(BankCardStatus.EXPIRED)
            .balance(BigDecimal.valueOf(1454.345))
            .build();
    bankCardRepository.save(existingCard);

    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getBankCardsPath()
                            + endpointsConfig.getAdminAccess()
                            + "/block/"
                            + existingCard.getId())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(
        BankCardStatusNotActiveException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void blockBankCardFailedIfBankCardNotFound() {
    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getBankCardsPath()
                            + endpointsConfig.getAdminAccess()
                            + "/block/"
                            + UUID.randomUUID())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(BankCardNotFoundException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void activateBankCard() {
    // GIVEN
    saveExistingUser();

    long number = 1234_1234_1234_1234L;
    String expectedNumber = "**** **** **** 1234";
    LocalDate expectedExpiryDate = LocalDate.now().plusMonths(1L);
    BankCardStatus expectedStatus = BankCardStatus.ACTIVE;
    BigDecimal expectedBalance = BigDecimal.valueOf(1454.345);

    BankCardEntity existingCard =
        BankCardEntity.builder()
            .number(number)
            .owner(existingUser)
            .expiryDate(expectedExpiryDate)
            .status(BankCardStatus.BLOCKED)
            .balance(expectedBalance)
            .build();
    bankCardRepository.save(existingCard);

    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getBankCardsPath()
                            + endpointsConfig.getAdminAccess()
                            + "/activate/"
                            + existingCard.getId())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    BankCardInfoDto actualCard = objectMapper.readValue(result, BankCardInfoDto.class);

    // THEN
    assertEquals(existingCard.getId(), actualCard.id());
    assertEquals(expectedNumber, actualCard.number());
    assertUserEquals(existingUser, actualCard.owner());
    assertEquals(expectedExpiryDate, actualCard.expiryDate());
    assertEquals(expectedStatus, actualCard.status());
    assertEquals(
        expectedBalance.setScale(4, RoundingMode.HALF_UP),
        actualCard.balance().setScale(4, RoundingMode.HALF_UP));
  }

  @Test
  @SneakyThrows
  void activateBankCardFailedIfBankCardNotFound() {
    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getBankCardsPath()
                            + endpointsConfig.getAdminAccess()
                            + "/activate/"
                            + UUID.randomUUID())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(BankCardNotFoundException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void deleteBankCardById() {
    // GIVEN
    saveExistingUser();

    long number = 1234_1234_1234_1234L;
    String expectedNumber = "**** **** **** 1234";
    LocalDate expectedExpiryDate = LocalDate.now().plusMonths(1L);
    BankCardStatus expectedStatus = BankCardStatus.EXPIRED;
    BigDecimal expectedBalance = BigDecimal.valueOf(1454.345);

    BankCardEntity existingCard =
        BankCardEntity.builder()
            .number(number)
            .owner(existingUser)
            .expiryDate(expectedExpiryDate)
            .status(expectedStatus)
            .balance(expectedBalance)
            .build();
    bankCardRepository.save(existingCard);

    // WHEN
    String result =
        mockMvc
            .perform(
                delete(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getBankCardsPath()
                            + endpointsConfig.getAdminAccess()
                            + "/"
                            + existingCard.getId())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    BankCardInfoDto actualCard = objectMapper.readValue(result, BankCardInfoDto.class);

    // THEN
    assertEquals(existingCard.getId(), actualCard.id());
    assertEquals(expectedNumber, actualCard.number());
    assertUserEquals(existingUser, actualCard.owner());
    assertEquals(expectedExpiryDate, actualCard.expiryDate());
    assertEquals(expectedStatus, actualCard.status());
    assertEquals(
        expectedBalance.setScale(4, RoundingMode.HALF_UP),
        actualCard.balance().setScale(4, RoundingMode.HALF_UP));
  }

  @Test
  @SneakyThrows
  void deleteBankCardByIdFailedIfBankCardNotFound() {
    // WHEN
    String result =
        mockMvc
            .perform(
                delete(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getBankCardsPath()
                            + endpointsConfig.getAdminAccess()
                            + "/"
                            + UUID.randomUUID())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(BankCardNotFoundException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void getBankCardById() {
    // GIVEN
    saveExistingUser();

    long number = 1234_1234_1234_1234L;
    String expectedNumber = "**** **** **** 1234";
    LocalDate expectedExpiryDate = LocalDate.now().plusMonths(1L);
    BankCardStatus expectedStatus = BankCardStatus.EXPIRED;
    BigDecimal expectedBalance = BigDecimal.valueOf(1454.345);

    BankCardEntity existingCard =
        BankCardEntity.builder()
            .number(number)
            .owner(existingUser)
            .expiryDate(expectedExpiryDate)
            .status(expectedStatus)
            .balance(expectedBalance)
            .build();
    bankCardRepository.save(existingCard);

    // WHEN
    String result =
        mockMvc
            .perform(
                get(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/"
                        + existingCard.getId())
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    BankCardInfoDto actualCard = objectMapper.readValue(result, BankCardInfoDto.class);

    // THEN
    assertEquals(existingCard.getId(), actualCard.id());
    assertEquals(expectedNumber, actualCard.number());
    assertUserEquals(existingUser, actualCard.owner());
    assertEquals(expectedExpiryDate, actualCard.expiryDate());
    assertEquals(expectedStatus, actualCard.status());
    assertEquals(
        expectedBalance.setScale(4, RoundingMode.HALF_UP),
        actualCard.balance().setScale(4, RoundingMode.HALF_UP));
  }

  @Test
  @SneakyThrows
  void getBankCardByIdFailedIfUserNotAccess() {
    // GIVEN
    saveExistingUser();

    UserEntity otherExistingUser =
        UserEntity.builder()
            .name("existing name")
            .password(passwordEncoder.encode("pass"))
            .email("other.existing_email@mail.em")
            .role(Role.USER)
            .build();
    userRepository.save(otherExistingUser);

    BankCardEntity existingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(otherExistingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.ONE)
            .build();
    bankCardRepository.save(existingCard);

    // WHEN
    String result =
        mockMvc
            .perform(
                get(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/"
                        + existingCard.getId())
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(UserNotAccessException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void getBankCardByIdFailedIfBankCardNotFound() {
    // GIVEN
    saveExistingUser();

    // WHEN
    String result =
        mockMvc
            .perform(
                get(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/"
                        + UUID.randomUUID())
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(BankCardNotFoundException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void transferBetweenOwnerCard() {
    // GIVEN
    saveExistingUser();

    BigDecimal amountTransfer = BigDecimal.TEN;
    BigDecimal existingSendingCardBalance = BigDecimal.valueOf(12123.12);
    BigDecimal expectedSendingCardBalance = existingSendingCardBalance.subtract(amountTransfer);
    BigDecimal existingReceivingCardBalance = BigDecimal.valueOf(1323123.12);
    BigDecimal expectedReceivingCardBalance = existingReceivingCardBalance.add(amountTransfer);

    BankCardEntity sendingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(existingSendingCardBalance)
            .build();
    bankCardRepository.save(sendingCard);

    BankCardEntity receivingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1235L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(existingReceivingCardBalance)
            .build();
    bankCardRepository.save(receivingCard);

    TransferDto transfer =
        new TransferDto(sendingCard.getId(), receivingCard.getId(), amountTransfer);

    // WHEN
    mockMvc
        .perform(
            post(endpointsConfig.getBasePath()
                    + endpointsConfig.getBankCardsPath()
                    + endpointsConfig.getUserAccess()
                    + "/transfer-self")
                .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(transfer)))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // THEN
    BankCardEntity actualSendingCard = bankCardRepository.findById(sendingCard.getId()).get();
    BankCardEntity actualReceivingCard = bankCardRepository.findById(receivingCard.getId()).get();

    assertEquals(
        expectedSendingCardBalance.setScale(4, RoundingMode.HALF_UP),
        actualSendingCard.getBalance().setScale(4, RoundingMode.HALF_UP));
    assertEquals(
        expectedReceivingCardBalance.setScale(4, RoundingMode.HALF_UP),
        actualReceivingCard.getBalance().setScale(4, RoundingMode.HALF_UP));
  }

  @Test
  @SneakyThrows
  void transferBetweenOwnerCardFailedIfReceivingBankCardNotActive() {
    // GIVEN
    saveExistingUser();

    BankCardEntity sendingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.valueOf(12123.12))
            .build();
    bankCardRepository.save(sendingCard);

    BankCardEntity receivingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1235L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.BLOCKED)
            .balance(BigDecimal.valueOf(1323123.12))
            .build();
    bankCardRepository.save(receivingCard);

    TransferDto transfer =
        new TransferDto(sendingCard.getId(), receivingCard.getId(), BigDecimal.ONE);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/transfer-self")
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(transfer)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(
        BankCardStatusNotActiveException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void transferBetweenOwnerCardFailedIfUserNotAccessToReceivingBankCard() {
    // GIVEN
    saveExistingUser();

    UserEntity otherExistingUser =
        UserEntity.builder()
            .name("existing name")
            .password(passwordEncoder.encode("pass"))
            .email("other.existing_email@mail.em")
            .role(Role.USER)
            .build();
    userRepository.save(otherExistingUser);

    BankCardEntity sendingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.valueOf(12123.12))
            .build();
    bankCardRepository.save(sendingCard);

    BankCardEntity receivingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1235L)
            .owner(otherExistingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.valueOf(1323123.12))
            .build();
    bankCardRepository.save(receivingCard);

    TransferDto transfer =
        new TransferDto(sendingCard.getId(), receivingCard.getId(), BigDecimal.ONE);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/transfer-self")
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(transfer)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(UserNotAccessException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void transferBetweenOwnerCardFailedIfReceivingBankCardNotFound() {
    // GIVEN
    saveExistingUser();

    BankCardEntity sendingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.valueOf(12123.12))
            .build();
    bankCardRepository.save(sendingCard);

    TransferDto transfer = new TransferDto(sendingCard.getId(), UUID.randomUUID(), BigDecimal.ONE);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/transfer-self")
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(transfer)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(BankCardNotFoundException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void transferBetweenOwnerCardFailedIfSendingBankCardBalanceNotEnough() {
    // GIVEN
    saveExistingUser();

    BigDecimal amountTransfer = BigDecimal.valueOf(1765.75);
    BigDecimal existingSendingCardBalance = amountTransfer.subtract(BigDecimal.TEN);

    BankCardEntity sendingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(existingSendingCardBalance)
            .build();
    bankCardRepository.save(sendingCard);

    BankCardEntity receivingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1235L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.BLOCKED)
            .balance(BigDecimal.valueOf(1323123.12))
            .build();
    bankCardRepository.save(receivingCard);

    TransferDto transfer =
        new TransferDto(sendingCard.getId(), receivingCard.getId(), amountTransfer);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/transfer-self")
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(transfer)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(
        NotEnoughBankCardBalanceException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void transferBetweenOwnerCardFailedIfSendingBankCardNotActive() {
    // GIVEN
    saveExistingUser();

    BankCardEntity sendingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.BLOCKED)
            .balance(BigDecimal.valueOf(12123.12))
            .build();
    bankCardRepository.save(sendingCard);

    BankCardEntity receivingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1235L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.valueOf(1323123.12))
            .build();
    bankCardRepository.save(receivingCard);

    TransferDto transfer =
        new TransferDto(sendingCard.getId(), receivingCard.getId(), BigDecimal.ONE);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/transfer-self")
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(transfer)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(
        BankCardStatusNotActiveException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void transferBetweenOwnerCardFailedIfUserNotAccessToSendingBankCard() {
    // GIVEN
    saveExistingUser();

    UserEntity otherExistingUser =
        UserEntity.builder()
            .name("existing name")
            .password(passwordEncoder.encode("pass"))
            .email("other.existing_email@mail.em")
            .role(Role.USER)
            .build();
    userRepository.save(otherExistingUser);

    BankCardEntity sendingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(otherExistingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.valueOf(12123.12))
            .build();
    bankCardRepository.save(sendingCard);

    BankCardEntity receivingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1235L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.valueOf(1323123.12))
            .build();
    bankCardRepository.save(receivingCard);

    TransferDto transfer =
        new TransferDto(sendingCard.getId(), receivingCard.getId(), BigDecimal.ONE);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/transfer-self")
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(transfer)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(UserNotAccessException.class.getSimpleName(), errorResponse.exceptionName());
  }

  @Test
  @SneakyThrows
  void transferBetweenOwnerCardFailedIfSendingBankCardNotFound() {
    // GIVEN
    saveExistingUser();

    BankCardEntity receivingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1235L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.valueOf(1323123.12))
            .build();
    bankCardRepository.save(receivingCard);

    TransferDto transfer =
        new TransferDto(UUID.randomUUID(), receivingCard.getId(), BigDecimal.ONE);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getBankCardsPath()
                        + endpointsConfig.getUserAccess()
                        + "/transfer-self")
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(transfer)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(BankCardNotFoundException.class.getSimpleName(), errorResponse.exceptionName());
  }

  private static void assertUserEquals(UserEntity expectedUser, UserInfoDto actualUser) {
    assertEquals(expectedUser.getId(), actualUser.id());
    assertEquals(expectedUser.getName(), actualUser.name());
    assertEquals(expectedUser.getEmail(), actualUser.email());
    assertEquals(expectedUser.getRole(), actualUser.role());
  }
}
