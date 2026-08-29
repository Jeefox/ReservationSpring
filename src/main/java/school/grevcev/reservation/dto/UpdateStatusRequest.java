package school.grevcev.reservation.dto;

import jakarta.validation.constraints.NotNull;
import school.grevcev.reservation.ReservationStatus;
import school.grevcev.reservation.validator.ValidEnum;

public record UpdateStatusRequest(
        @NotNull(message = "Status cannot be null")
        @ValidEnum(enumClass = ReservationStatus.class, message = "Invalid status value")
        ReservationStatus status) {
}
