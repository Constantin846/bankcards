package tk.project.bankcards.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import tk.project.bankcards.dto.ErrorResponse;
import tk.project.bankcards.dto.UserInfoDto;
import tk.project.bankcards.dto.UserUpdateDto;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.enums.Role;
import tk.project.bankcards.exception.UserConflictException;
import tk.project.bankcards.exception.UserNotFoundException;

class UserIntegrationTest extends BaseIntegrationTest {

  @Test
  @SneakyThrows
  void getAuthUser() {
    // GIVEN
    saveExistingUser();

    // WHEN
    String result =
        mockMvc
            .perform(
                get(endpointsConfig.getBasePath()
                        + endpointsConfig.getUsersPath()
                        + endpointsConfig.getUserAccess())
                    .with(httpBasic(existingUser.getUsername(), existingUserPassword))
                    .contentType("application/json"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserInfoDto actualUser = objectMapper.readValue(result, UserInfoDto.class);

    // THEN
    assertEquals(existingUser.getId(), actualUser.id());
    assertEquals(existingUser.getName(), actualUser.name());
    assertEquals(existingUser.getEmail(), actualUser.email());
    assertEquals(existingUser.getRole(), actualUser.role());
  }

  @Test
  @SneakyThrows
  void updateUser() {
    // GIVEN
    saveExistingUser();

    UUID expectedId = existingUser.getId();
    String expectedName = "new name";
    String expectedEmail = "newEmail@mail.em";
    Role expectedRole = Role.USER;
    UserUpdateDto userUpdateDto =
        new UserUpdateDto(expectedId, expectedName, "new password", expectedEmail);

    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getUsersPath()
                            + endpointsConfig.getAdminAccess())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(userUpdateDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserInfoDto actualUser = objectMapper.readValue(result, UserInfoDto.class);

    // THEN
    assertEquals(expectedId, actualUser.id());
    assertEquals(expectedName, actualUser.name());
    assertEquals(expectedEmail, actualUser.email());
    assertEquals(expectedRole, actualUser.role());
  }

  @Test
  @SneakyThrows
  void updateUserFailedIfUserNotFound() {
    // GIVEN
    UserUpdateDto userUpdateDto =
        new UserUpdateDto(UUID.randomUUID(), "name", "new password", null);

    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getUsersPath()
                            + endpointsConfig.getAdminAccess())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(userUpdateDto)))
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
  void notUpdateUserFieldsToNull() {
    // GIVEN
    saveExistingUser();

    UUID expectedId = existingUser.getId();
    String expectedName = existingUser.getName();
    String expectedEmail = existingUser.getEmail();
    Role expectedRole = existingUser.getRole();

    UserUpdateDto userUpdateDto = new UserUpdateDto(expectedId, null, null, null);

    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getUsersPath()
                            + endpointsConfig.getAdminAccess())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(userUpdateDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserInfoDto actualUser = objectMapper.readValue(result, UserInfoDto.class);

    // THEN
    assertEquals(expectedId, actualUser.id());
    assertEquals(expectedName, actualUser.name());
    assertEquals(expectedEmail, actualUser.email());
    assertEquals(expectedRole, actualUser.role());
  }

  @Test
  @SneakyThrows
  void notUpdateUserFieldsToBlank() {
    // GIVEN
    saveExistingUser();

    UUID expectedId = existingUser.getId();
    String expectedName = existingUser.getName();
    String expectedEmail = existingUser.getEmail();
    Role expectedRole = existingUser.getRole();

    UserUpdateDto userUpdateDto = new UserUpdateDto(expectedId, "    ", "          ", null);

    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getUsersPath()
                            + endpointsConfig.getAdminAccess())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(userUpdateDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserInfoDto actualUser = objectMapper.readValue(result, UserInfoDto.class);

    // THEN
    assertEquals(expectedId, actualUser.id());
    assertEquals(expectedName, actualUser.name());
    assertEquals(expectedEmail, actualUser.email());
    assertEquals(expectedRole, actualUser.role());
  }

  @Test
  @SneakyThrows
  void updateUserFailedIfUserEmailAlreadyExists() {
    // GIVEN
    String otherExistingEmail = "other.existing_email@mail.em";
    UserEntity otherExistingUser =
        UserEntity.builder()
            .name("existing name")
            .password(passwordEncoder.encode("pass"))
            .email(otherExistingEmail)
            .role(Role.USER)
            .build();
    userRepository.save(otherExistingUser);

    saveExistingUser();
    UserUpdateDto userUpdateDto =
        new UserUpdateDto(existingUser.getId(), "new name", "new password", otherExistingEmail);

    // WHEN
    String result =
        mockMvc
            .perform(
                patch(
                        endpointsConfig.getBasePath()
                            + endpointsConfig.getUsersPath()
                            + endpointsConfig.getAdminAccess())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(userUpdateDto)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ErrorResponse errorResponse = objectMapper.readValue(result, ErrorResponse.class);

    // THEN
    assertEquals(UserConflictException.class.getSimpleName(), errorResponse.exceptionName());
  }
}
