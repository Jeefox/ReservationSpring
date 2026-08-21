package school.grevcev.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.grevcev.reservation.ReservationStatus;
import school.grevcev.reservation.dto.CreateReservationRequest;
import school.grevcev.reservation.dto.ReservationResponse;
import school.grevcev.reservation.exception.ReservationNotFoundException;
import school.grevcev.reservation.exception.RoomAlreadyBookedException;
import school.grevcev.reservation.exception.RoomNotFoundException;
import school.grevcev.reservation.exception.UserNotFoundException;
import school.grevcev.reservation.model.Reservation;
import school.grevcev.reservation.model.Room;
import school.grevcev.reservation.model.User;
import school.grevcev.reservation.repository.ReservationRepository;
import school.grevcev.reservation.repository.RoomRepository;
import school.grevcev.reservation.repository.UserRepository;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public ReservationService (ReservationRepository reservationRepository, UserRepository userRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(()->new ReservationNotFoundException(id));
        return toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAll(){
        List<Reservation> reservations = reservationRepository.findAllWithAssociations();
    return reservations.stream().map(this::toResponse).toList();
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
    public ReservationResponse updateReservation(Long id, CreateReservationRequest request) {
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
}
