package tk.project.bankcards.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "DTO для информации об ошибке")
public record ErrorResponse(
    @Schema(description = "Имя ошибки", requiredMode = REQUIRED) String exceptionName,
    @Schema(description = "Время отправки ошибки", requiredMode = REQUIRED) Instant timestamp,
    @Schema(description = "Описание ошибки", requiredMode = REQUIRED) String errorMessage) {}
