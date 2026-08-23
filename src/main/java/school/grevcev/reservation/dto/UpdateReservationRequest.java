package school.grevcev.reservation.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import school.grevcev.reservation.ReservationStatus;

import java.time.LocalDate;

public record UpdateReservationRequest(
        @NotNull
        Long userId,
        @NotNull
        Long roomId,
        @NotNull
        LocalDate startDate,
        @NotNull
        LocalDate endDate,
        @NotNull
        ReservationStatus status) {
    @AssertTrue
    public boolean isDateValid(){
        if(startDate == null || endDate == null) return true;
        return !endDate.isBefore(startDate);
    }
}


