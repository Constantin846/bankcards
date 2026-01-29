package tk.project.bankcards.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import tk.project.bankcards.dto.ErrorResponse;
import tk.project.bankcards.entity.BankCardEntity;
import tk.project.bankcards.entity.RequestEntity;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.enums.BankCardStatus;
import tk.project.bankcards.enums.RequestAction;
import tk.project.bankcards.enums.RequestStatus;
import tk.project.bankcards.enums.Role;
import tk.project.bankcards.exception.BankCardNotFoundException;
import tk.project.bankcards.exception.BankCardStatusNotActiveException;
import tk.project.bankcards.exception.UserNotAccessException;

class RequestIntegrationTest extends BaseIntegrationTest {

  @Test
  @SneakyThrows
  void createBlockCardRequest() {
    // GIVEN
    saveExistingUser();

    BankCardEntity existingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.ACTIVE)
            .balance(BigDecimal.ONE)
            .build();
    bankCardRepository.save(existingCard);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getRequestsPath()
                        + endpointsConfig.getUserAccess()
                        + "/block/"
                        + existingCard.getId())
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Map<String, UUID> requestId =
        objectMapper.readValue(result, new TypeReference<Map<String, UUID>>() {});
    Optional<RequestEntity> savedRequestOp = requestRepository.findById(requestId.get("requestId"));

    // THEN
    assertTrue(savedRequestOp.isPresent());
    RequestEntity savedRequest = savedRequestOp.get();

    assertEquals(existingUser.getId(), savedRequest.getOwner().getId());
    assertEquals(existingCard.getId(), savedRequest.getBankCardId());
    assertEquals(RequestAction.BLOCK_BANK_CARD, savedRequest.getAction());
    assertEquals(RequestStatus.PENDING, savedRequest.getStatus());
  }

  @Test
  @SneakyThrows
  void createBlockCardRequestFailedIfBankCardIsNotActive() {
    // GIVEN
    saveExistingUser();

    BankCardEntity existingCard =
        BankCardEntity.builder()
            .number(1234_1234_1234_1234L)
            .owner(existingUser)
            .expiryDate(LocalDate.now().plusMonths(2L))
            .status(BankCardStatus.BLOCKED)
            .balance(BigDecimal.ONE)
            .build();
    bankCardRepository.save(existingCard);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getRequestsPath()
                        + endpointsConfig.getUserAccess()
                        + "/block/"
                        + existingCard.getId())
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
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
  void createBlockCardRequestFailedIfUserNotAccess() {
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
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getRequestsPath()
                        + endpointsConfig.getUserAccess()
                        + "/block/"
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
  void createBlockCardRequestFailedIfBankCardNotFound() {
    // GIVEN
    saveExistingUser();

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath()
                        + endpointsConfig.getRequestsPath()
                        + endpointsConfig.getUserAccess()
                        + "/block/"
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
}
