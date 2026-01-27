package tk.project.bankcards.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO для запроса на создание банковской карты")
public record BankCardCreateDto(
    @NotNull
        @Min(value = 1000_0000_0000_0000L)
        @Max(value = 9999_9999_9999_9999L)
        @Schema(description = "Номер карты", requiredMode = REQUIRED)
        Long number,
    @NotNull @Schema(description = "Id владельца карты", requiredMode = REQUIRED) UUID ownerId,
    @NotNull
        @Future
        @JsonFormat(pattern = "dd-MM-yyyy")
        @Schema(
            description = "Срок действия карты",
            example = "15-12-2027",
            requiredMode = REQUIRED)
        LocalDate expiryDate,
    @NotNull @Schema(description = "Баланс карты", requiredMode = REQUIRED) BigDecimal balance) {}
