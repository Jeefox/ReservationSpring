package school.grevcev.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotNull
        String name,
        @NotNull
        @Email
        String email
) {
}
