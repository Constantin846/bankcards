package tk.project.bankcards.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO для запроса на регистрацию пользователя")
public record UserRegisterDto(
    @NotBlank
        @Size(min = 1, max = 20)
        @Schema(description = "Имя пользователя", requiredMode = REQUIRED)
        String name,
    @NotBlank
        @Size(min = 8, max = 30)
        @Schema(description = "Пароль пользователя", requiredMode = REQUIRED)
        String password,
    @NotBlank
        @Size(min = 1, max = 30)
        @Email
        @Schema(description = "Email пользователя", requiredMode = REQUIRED)
        String email) {}
