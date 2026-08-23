package school.grevcev.reservation.dto;

public record RoomResponse(
        Long id,
        String name,
        int capacity
) {
}
