package tk.project.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tk.project.bankcards.dto.UserInfoDto;
import tk.project.bankcards.dto.UserRegisterDto;
import tk.project.bankcards.service.UserService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${app.controller.base-path}" + "${app.controller.registration-path}")
@Tag(name = "RegistrationController", description = "API для регистрации пользователей")
public class RegistrationController {

  private final UserService userService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Регистрация нового пользователя с указанным именем, почтой и паролем")
  public UserInfoDto register(@Valid @RequestBody UserRegisterDto user) {
    log.info(
        "Получен запрос на регистрацию пользователя с именем {} и почтой {}.",
        user.name(),
        user.email());

    UserInfoDto savedUser = userService.register(user);

    log.info(
        "Выполнен запрос на регистрацию пользователя с id {}, с именем {} и почтой {}.",
        savedUser.id(),
        savedUser.name(),
        savedUser.email());
    return savedUser;
  }
}
