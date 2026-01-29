package tk.project.bankcards.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import tk.project.bankcards.dto.ErrorResponse;
import tk.project.bankcards.dto.UserInfoDto;
import tk.project.bankcards.dto.UserRegisterDto;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.enums.Role;
import tk.project.bankcards.exception.UserConflictException;

class RegistrationIntegrationTest extends BaseIntegrationTest {

  @Test
  @SneakyThrows
  void registerUser() {
    // GIVEN
    Role expectedUserRole = Role.USER;
    String expectedUserName = "name";
    String expectedUserEmail = "email@mail";
    String password = "password";
    UserRegisterDto userRegisterDto =
        new UserRegisterDto(expectedUserName, password, expectedUserEmail);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath() + endpointsConfig.getRegistrationPath())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(userRegisterDto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserInfoDto actualUser = objectMapper.readValue(result, UserInfoDto.class);

    // THEN
    assertNotNull(actualUser.id());
    assertEquals(expectedUserName, actualUser.name());
    assertEquals(expectedUserEmail, actualUser.email());
    assertEquals(expectedUserRole, actualUser.role());
  }

  @Test
  @SneakyThrows
  void registerUserFailedIfUserEmailAlreadyExists() {
    // GIVEN
    String existingEmail = "existing_email@mail.em";
    UserEntity existingUser =
        UserEntity.builder()
            .name("existing name")
            .password(passwordEncoder.encode("pass"))
            .email(existingEmail)
            .role(Role.USER)
            .build();
    userRepository.save(existingUser);

    UserRegisterDto userRegisterDto = new UserRegisterDto("name", "password", existingEmail);

    // WHEN
    String result =
        mockMvc
            .perform(
                post(endpointsConfig.getBasePath() + endpointsConfig.getRegistrationPath())
                    .with(httpBasic(admin.getUsername(), adminPassword))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(userRegisterDto)))
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
