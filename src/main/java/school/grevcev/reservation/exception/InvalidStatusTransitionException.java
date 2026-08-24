package school.grevcev.reservation.exception;

import school.grevcev.reservation.ReservationStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(ReservationStatus oldStatus, ReservationStatus newStatus) {
        super("Cannot transition status from " + oldStatus + " to " + newStatus);
    }
}
