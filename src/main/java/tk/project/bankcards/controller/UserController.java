package tk.project.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.project.bankcards.dto.UserInfoDto;
import tk.project.bankcards.dto.UserUpdateDto;
import tk.project.bankcards.service.UserService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${app.controller.base-path}" + "${app.controller.users-path}")
@Tag(name = "UserController", description = "API для получения информации о пользователе")
public class UserController {

  private final UserService userService;

  @GetMapping("${app.controller.user-access}")
  @Operation(summary = "Получение информации об аутентифицированном пользователе")
  public UserInfoDto login(@AuthenticationPrincipal UserDetails user) {
    log.info("Запрос на получение данных пользователя с почтой: {}", user.getUsername());
    return userService.getUserByEmail(user.getUsername());
  }

  @PatchMapping("${app.controller.admin-access}")
  @Operation(summary = "Обновление данных пользователя")
  public UserInfoDto update(@Valid @RequestBody UserUpdateDto user) {
    log.info(
        "Получен запрос на обновление пользователя с id {}, с именем {} и почтой {}.",
        user.id(),
        user.name(),
        user.email());

    UserInfoDto updatedUser = userService.update(user);

    log.info(
        "Выполнен запрос на обновление пользователя с id {}, с именем {} и почтой {}.",
        updatedUser.id(),
        updatedUser.name(),
        updatedUser.email());
    return updatedUser;
  }
}
