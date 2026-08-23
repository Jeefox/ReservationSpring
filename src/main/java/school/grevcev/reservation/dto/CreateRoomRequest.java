package school.grevcev.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRoomRequest (
    @NotNull
    String name,
    @NotNull
    @Positive
    Integer capacity
){
}
