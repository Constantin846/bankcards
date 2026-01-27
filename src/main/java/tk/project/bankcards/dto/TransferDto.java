package tk.project.bankcards.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO для запроса на перевод средств между картами")
public record TransferDto(
    @NotNull
        @Schema(description = "Id карты отправления", requiredMode = REQUIRED)
        UUID fromCardId,
    @NotNull
        @Schema(description = "Id карты получения", requiredMode = REQUIRED)
        UUID toCardId,
    @NotNull @Positive @Schema(description = "Сумма перевода", requiredMode = REQUIRED)
        BigDecimal amount) {}
