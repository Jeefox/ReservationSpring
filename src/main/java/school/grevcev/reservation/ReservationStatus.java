package school.grevcev.reservation;

public enum ReservationStatus {
    PENDING,
    APPROVED,
    CANCELLED;

    public boolean canTransitionTo(ReservationStatus target){
        return switch (this){
            case PENDING -> target == APPROVED || target == CANCELLED;
            case APPROVED -> target == CANCELLED;
            case CANCELLED -> false;
        };
    }
}
