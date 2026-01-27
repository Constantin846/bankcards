package tk.project.bankcards.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;
import tk.project.bankcards.enums.BankCardStatus;

@Schema(description = "DTO для краткой информации о банковской карте")
public record BankCardShortInfoDto(
    @Schema(description = "Id карты", requiredMode = REQUIRED) UUID id,
    @Schema(description = "Номер карты", example = "**** **** **** 1234", requiredMode = REQUIRED)
        String number,
    @Schema(description = "Владелец карты", requiredMode = REQUIRED) UUID ownerId,
    @JsonFormat(pattern = "dd-MM-yyyy")
        @Schema(
            description = "Срок действия карты",
            example = "15-12-2025",
            requiredMode = REQUIRED)
        LocalDate expiryDate,
    @Schema(description = "Статус карты", requiredMode = REQUIRED) BankCardStatus status) {}
