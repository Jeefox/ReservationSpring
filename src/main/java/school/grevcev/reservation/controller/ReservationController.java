package school.grevcev.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.grevcev.reservation.ReservationStatus;
import school.grevcev.reservation.dto.CreateReservationRequest;
import school.grevcev.reservation.dto.PageResponse;
import school.grevcev.reservation.dto.ReservationResponse;
import school.grevcev.reservation.dto.UpdateReservationRequest;
import school.grevcev.reservation.service.ReservationService;

import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest createReservationRequest) {
        ReservationResponse reservationResponse = reservationService.createReservation(createReservationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponse);
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @PutMapping("/{id}")
    public ReservationResponse updateReservation(@PathVariable Long id, @Valid @RequestBody UpdateReservationRequest reservation) {
        return reservationService.updateReservation(id, reservation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationById(@PathVariable Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public PageResponse<ReservationResponse> searchReservations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable
            ) {
        return PageResponse.from(reservationService.search(userId, roomId, status, from, to, pageable));
    }
}
