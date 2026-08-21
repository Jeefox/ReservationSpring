package school.grevcev.reservation.dto;

import school.grevcev.reservation.ReservationStatus;

import java.time.LocalDate;

public record ReservationRequest(
        String customerName,
        LocalDate reservationDate,
        ReservationStatus status
) {
}
