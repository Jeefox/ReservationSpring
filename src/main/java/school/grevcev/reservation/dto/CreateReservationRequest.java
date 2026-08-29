package school.grevcev.reservation.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import school.grevcev.reservation.ReservationStatus;

import java.time.LocalDate;

public record CreateReservationRequest(
        @NotNull Long roomId,
        @NotNull@FutureOrPresent LocalDate startDate,
        @NotNull LocalDate endDate) {
    @AssertTrue
    public boolean isDateValid(){
        if(startDate ==null || endDate ==null) return true;
        return !endDate.isBefore(startDate);
    }
}
