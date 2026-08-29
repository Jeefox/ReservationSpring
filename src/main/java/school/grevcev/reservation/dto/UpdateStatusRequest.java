package school.grevcev.reservation.dto;

import jakarta.validation.constraints.NotNull;
import school.grevcev.reservation.ReservationStatus;

public record UpdateStatusRequest(
        @NotNull
        ReservationStatus status) {
}
