package tk.project.bankcards.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import tk.project.bankcards.enums.Role;

@Schema(description = "DTO для информации о пользователе")
public record UserInfoDto(
    @Schema(description = "Id пользователя", requiredMode = REQUIRED) UUID id,
    @Schema(description = "Имя пользователя", requiredMode = REQUIRED) String name,
    @Schema(description = "Email пользователя", requiredMode = REQUIRED) String email,
    @Schema(description = "Роль пользователя", requiredMode = REQUIRED) Role role) {}
