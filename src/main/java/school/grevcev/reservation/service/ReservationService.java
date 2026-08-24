package school.grevcev.reservation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.grevcev.reservation.ReservationStatus;
import school.grevcev.reservation.dto.CreateReservationRequest;
import school.grevcev.reservation.dto.ReservationResponse;
import school.grevcev.reservation.dto.UpdateReservationRequest;
import school.grevcev.reservation.dto.UpdateStatusRequest;
import school.grevcev.reservation.event.ReservationStatusChangedEvent;
import school.grevcev.reservation.event.ReservationCreatedEvent;
import school.grevcev.reservation.exception.*;
import school.grevcev.reservation.model.Reservation;
import school.grevcev.reservation.model.ReservationSpecifications;
import school.grevcev.reservation.model.Room;
import school.grevcev.reservation.model.User;
import school.grevcev.reservation.repository.ReservationRepository;
import school.grevcev.reservation.repository.RoomRepository;
import school.grevcev.reservation.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ReservationService (ReservationRepository reservationRepository, UserRepository userRepository,
                               RoomRepository roomRepository,  ApplicationEventPublisher applicationEventPublisher) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(()->new ReservationNotFoundException(id));
        return toResponse(reservation);
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        User user = userRepository.findById(request.userId()).orElseThrow(()-> new UserNotFoundException(request.userId()));
        Room room = roomRepository.findById(request.roomId()).orElseThrow(()->new RoomNotFoundException(request.roomId()));

        List<Reservation> conflicting = reservationRepository.findConflictingReservations(room.getId(), request.startDate(),
                request.endDate(), ReservationStatus.CANCELLED, null);
        if(!conflicting.isEmpty()) throw new RoomAlreadyBookedException(room.getId(), request.startDate(), request.endDate());

        Reservation reservation = Reservation.builder()
                .user(user)
                .room(room)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(request.status())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        applicationEventPublisher.publishEvent(new ReservationCreatedEvent(savedReservation.getId(),
                savedReservation.getRoom().getId(), savedReservation.getStartDate(), savedReservation.getEndDate()));

        return toResponse(savedReservation);
    }

    private ReservationResponse toResponse(Reservation savedReservation) {
        return new ReservationResponse(
                savedReservation.getId(),
                savedReservation.getUser().getId(),
                savedReservation.getUser().getName(),
                savedReservation.getRoom().getId(),
                savedReservation.getRoom().getName(),
                savedReservation.getStartDate(),
                savedReservation.getEndDate(),
                savedReservation.getStatus()
        );
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, UpdateReservationRequest request) {
        Reservation foundReservation = reservationRepository.findById(id).orElseThrow(()-> new ReservationNotFoundException(id));
        User user = userRepository.findById(request.userId()).orElseThrow(()-> new UserNotFoundException(request.userId()));
        Room room = roomRepository.findById(request.roomId()).orElseThrow(()-> new RoomNotFoundException(request.roomId()));

        List<Reservation> conflicting = reservationRepository.findConflictingReservations(request.roomId(), request.startDate(),
                request.endDate(), ReservationStatus.CANCELLED, id);

        if(!conflicting.isEmpty()) throw new RoomAlreadyBookedException(request.roomId(), request.startDate(), request.endDate());

        foundReservation.setUser(user);
        foundReservation.setRoom(room);
        foundReservation.setStartDate(request.startDate());
        foundReservation.setEndDate(request.endDate());
        foundReservation.setStatus(request.status());

        return toResponse(foundReservation);
    }

    @Transactional
    public void deleteReservationById(Long id) {
        Reservation foundReservation = reservationRepository.findById(id).orElseThrow(()-> new ReservationNotFoundException(id));
        reservationRepository.delete(foundReservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> search(Long userId, Long roomId, ReservationStatus status, LocalDate from, LocalDate to, Pageable pageable){
        List<Specification<Reservation>> specifications = new ArrayList<>();
        if(userId != null) specifications.add(ReservationSpecifications.hasUserId(userId));
        if(roomId != null) specifications.add(ReservationSpecifications.hasRoomId(roomId));
        if(status != null) specifications.add(ReservationSpecifications.hasStatus(status));
        if (from != null && to != null) specifications.add(ReservationSpecifications.overlapsWith(from, to));

        return  reservationRepository.findAll(Specification.allOf(specifications), pageable).map(this::toResponse);
    }

    @Transactional
    public ReservationResponse changeStatus(Long id, UpdateStatusRequest request) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(()-> new ReservationNotFoundException(id));

        ReservationStatus currentStatus = reservation.getStatus();
        if(!currentStatus.canTransitionTo(request.status())) {
            throw new InvalidStatusTransitionException(currentStatus, request.status());
        }
            reservation.setStatus(request.status());
            log.info("Reservation {} status changed: {} -> {}", id, currentStatus, request.status());

            applicationEventPublisher.publishEvent(new ReservationStatusChangedEvent(reservation.getId(), currentStatus, request.status()));
            return toResponse(reservation);
    }
}
