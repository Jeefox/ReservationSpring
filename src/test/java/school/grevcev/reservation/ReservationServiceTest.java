package school.grevcev.reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import school.grevcev.reservation.service.ReservationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void createReservation_success() {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(
                1L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
                ReservationStatus.PENDING
        );

        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room));

        Reservation saved = Reservation.builder().id(10L).user(user).room(room)
                .startDate(createReservationRequest.startDate()).endDate(createReservationRequest.endDate())
                .status(createReservationRequest.status()).build();

        when(reservationRepository.save(any())).thenReturn(saved);

        ReservationResponse response = reservationService.createReservation(createReservationRequest);

        assertEquals(10L, response.id());
        assertEquals("Ivan", response.userName());
        assertEquals("luxury", response.roomName());
        verify(reservationRepository).save(any());
    }

    @Test
    void createReservation_userNotFound() {
        CreateReservationRequest request = new CreateReservationRequest(
                999L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
                ReservationStatus.PENDING
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, ()-> reservationService.createReservation(request));
    }

    @Test
    void getReservationById_success(){
       User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
       Room room = Room.builder().id(2L).name("luxury").capacity(2).build();
       Reservation reservation = Reservation.builder().id(10L).user(user).room(room)
               .startDate(LocalDate.now().plusDays(1))
               .endDate(LocalDate.now().plusDays(3))
               .status(ReservationStatus.PENDING)
               .build();

       when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

       ReservationResponse response = reservationService.getReservationById(10L);

       assertEquals(10L, response.id());
       assertEquals("Ivan", response.userName());
    }

    @Test
    void getReservationById_notFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, ()-> reservationService.getReservationById(999L));
    }

    @Test
    void createReservation_roomNotFound() {
        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        CreateReservationRequest request = new CreateReservationRequest(
                1L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
                ReservationStatus.PENDING
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, ()-> reservationService.createReservation(request));
    }

    @Test
    void deleteReservation_success() {
        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();
        Reservation reservation = Reservation.builder().id(10L).user(user).room(room)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

        reservationService.deleteReservationById(10L);

        verify(reservationRepository).delete(reservation);
    }

    @Test
    void deleteReservation_notFound() {

        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, ()-> reservationService.deleteReservationById(999L));

        verify(reservationRepository, never()).delete(any());
    }

    @Test
    void createReservation_roomAlreadyBooked() {
        CreateReservationRequest request = new CreateReservationRequest(
                1L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
                ReservationStatus.PENDING
        );

        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();

        User otherUser = User.builder().id(99L).name("Other").email("other@email.com").build();
        Reservation conflictingReservation = Reservation.builder()
                .id(5L).user(otherUser).room(room)
                .startDate(LocalDate.now().plusDays(2))  // пересечение дат
                .endDate(LocalDate.now().plusDays(5))
                .status(ReservationStatus.PENDING)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room));

        when(reservationRepository.findConflictingReservations(request.roomId(), request.startDate(),
                request.endDate(), ReservationStatus.CANCELLED, null)).thenReturn(List.of(conflictingReservation));

        assertThrows(RoomAlreadyBookedException.class, ()-> reservationService.createReservation(request));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_noConflict() {
        CreateReservationRequest request = new CreateReservationRequest(
                1L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
                ReservationStatus.PENDING
        );

        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room));

        // Мокаем возврат ПУСТОГО списка (нет конфликтов)
        when(reservationRepository.findConflictingReservations(
                2L, request.startDate(), request.endDate(),
                ReservationStatus.CANCELLED, null))
                .thenReturn(List.of());  // ← просто пустой список, без Optional!

        Reservation saved = Reservation.builder().id(10L).user(user).room(room)
                .startDate(request.startDate()).endDate(request.endDate())
                .status(request.status()).build();
        when(reservationRepository.save(any())).thenReturn(saved);

        // WHEN
        ReservationResponse response = reservationService.createReservation(request);

        // THEN
        assertEquals(10L, response.id());
        verify(reservationRepository).save(any());
    }


}
