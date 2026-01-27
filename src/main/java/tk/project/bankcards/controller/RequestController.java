package tk.project.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.service.RequestService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${app.controller.base-path}" + "${app.controller.requests-path}")
@Tag(name = "RequestController", description = "API для работы с запросами пользователя")
public class RequestController {

  private final RequestService requestService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("${app.controller.user-access}/block/{cardId}")
  @Operation(summary = "Создание запроса на блокировку карты")
  public Map<String, UUID> createBlockCardRequest(
      @PathVariable("cardId") UUID cardId, @AuthenticationPrincipal UserEntity authUser) {

    log.info("Получен запрос на блокировку банковской карты с id {}.", cardId);

    UUID createdRequestId = requestService.createBlockCardRequest(cardId, authUser);

    log.info(
        "Выполнен запрос на блокировку банковской карты с id {}, id запроса {}.",
        cardId,
        createdRequestId);
    return Map.of("requestId", createdRequestId);
  }
}
