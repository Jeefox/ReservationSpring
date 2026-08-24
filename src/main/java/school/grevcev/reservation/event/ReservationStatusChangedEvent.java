package school.grevcev.reservation.event;

import school.grevcev.reservation.ReservationStatus;

public record ReservationStatusChangedEvent(
        Long reservationId, ReservationStatus from, ReservationStatus to
) {
}
