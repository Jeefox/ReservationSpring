package school.grevcev.reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.grevcev.reservation.dto.CreateReservationRequest;
import school.grevcev.reservation.dto.ReservationResponse;
import school.grevcev.reservation.dto.UpdateReservationRequest;
import school.grevcev.reservation.dto.UpdateStatusRequest;
import school.grevcev.reservation.exception.*;
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

        verify(reservationRepository, never()).delete(any(Reservation.class));
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

    @Test
    void updateReservation_sameReservationNoConflict() {

        UpdateReservationRequest request = new UpdateReservationRequest(
                1L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                ReservationStatus.PENDING
        );

        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();
        Reservation reservation = Reservation.builder()
                .id(10L)
                .user(user)
                .room(room)
                .status(ReservationStatus.PENDING)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room));
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

        when(reservationRepository.findConflictingReservations(
                2L, request.startDate(), request.endDate(),
                ReservationStatus.CANCELLED, 10L))
                .thenReturn(List.of());  // ← просто пустой список, без Optional!

        reservationService.updateReservation(10L, request);

        verify(reservationRepository).findConflictingReservations(
                eq(2L), any(), any(), eq(ReservationStatus.CANCELLED), eq(10L));
    }

    @Test
    void updateReservation_conflictWithOver(){
        UpdateReservationRequest request = new UpdateReservationRequest(
                3L, 4L, LocalDate.now().plusDays(2), LocalDate.now().plusDays(4),
                ReservationStatus.PENDING
        );

        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();
        Reservation reservation = Reservation.builder()
                .id(10L)
                .user(user)
                .room(room)
                .status(ReservationStatus.PENDING)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .build();

        User newUser = User.builder().id(3L).name("NewUser").email("new@email.com").build();
        Room newRoom = Room.builder().id(4L).name("newRoom").capacity(3).build();

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newUser));
        when(roomRepository.findById(4L)).thenReturn(Optional.of(newRoom));

        User otherUser = User.builder().id(99L).name("Other").email("other@email.com").build();
        Reservation conflicting = Reservation.builder()
                .id(5L).user(otherUser).room(newRoom)
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findConflictingReservations(
                4L, request.startDate(), request.endDate(),
                ReservationStatus.CANCELLED, 10L)).thenReturn(List.of(conflicting));

        assertThrows(RoomAlreadyBookedException.class, ()-> reservationService.updateReservation(10L, request));
    }

    @Test
    void changeStatus_pendingToApproved_success(){
        UpdateStatusRequest request = new UpdateStatusRequest(ReservationStatus.APPROVED);
        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();

        Reservation reservation =  Reservation.builder()
                .id(1L)
                .user(user)
                .room(room)
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.changeStatus(1L, request);

        assertEquals(ReservationStatus.APPROVED, reservation.getStatus());
        assertEquals(ReservationStatus.APPROVED, response.status());
        assertEquals(1L, response.id());
    }

    @Test
    void changeStatus_pendingToCancelled_success(){
        UpdateStatusRequest request = new UpdateStatusRequest(ReservationStatus.CANCELLED);
        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();

        Reservation reservation =  Reservation.builder()
                .id(1L)
                .user(user)
                .room(room)
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.changeStatus(1L, request);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(ReservationStatus.CANCELLED, response.status());
        assertEquals(1L, response.id());
    }

    @Test
    void changeStatus_approvedToCancelled_success(){
        UpdateStatusRequest request = new UpdateStatusRequest(ReservationStatus.CANCELLED);
        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();

        Reservation reservation =  Reservation.builder()
                .id(1L)
                .user(user)
                .room(room)
                .status(ReservationStatus.APPROVED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.changeStatus(1L, request);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(ReservationStatus.CANCELLED, response.status());
        assertEquals(1L, response.id());
    }

    @Test
    void changeStatus_cancelledToApproved_throws(){
        UpdateStatusRequest request = new UpdateStatusRequest(ReservationStatus.APPROVED);
        User user = User.builder().id(1L).name("Ivan").email("ivan@email.com").build();
        Room room = Room.builder().id(2L).name("luxury").capacity(2).build();

        Reservation reservation =  Reservation.builder()
                .id(1L)
                .user(user)
                .room(room)
                .status(ReservationStatus.CANCELLED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidStatusTransitionException.class, ()-> reservationService.changeStatus(1L, request));

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }
}
