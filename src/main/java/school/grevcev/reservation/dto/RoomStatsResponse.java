package school.grevcev.reservation.dto;

public record RoomStatsResponse(
        Long roomId,
        String roomName,
        Long bookingCount
) {
}
