package school.grevcev.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        String name,
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 8, message = "Пароль минимум 8 символов")
        String password
) {
}
