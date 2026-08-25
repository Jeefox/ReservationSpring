package school.grevcev.reservation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import school.grevcev.reservation.ReservationStatus;
import school.grevcev.reservation.dto.ReservationResponse;
import school.grevcev.reservation.exception.InvalidStatusTransitionException;
import school.grevcev.reservation.exception.ReservationNotFoundException;
import school.grevcev.reservation.service.ReservationService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void createReservation_returns201WithLocation() throws Exception {
        when(reservationService.createReservation(any())).thenReturn(
                new ReservationResponse(10L, 1L, "Ivan", 2L, "luxury",
                        LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5),
                        ReservationStatus.PENDING));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":1,"roomId":2,
                                 "startDate":"2026-12-01","endDate":"2026-12-05",
                                 "status":"PENDING"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userName").value("Ivan"));
    }

    @Test
    void createReservation_returns400WithNoEndDate() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":1,"roomId":2,
                                 "startDate":"2026-12-01","endDate":null,
                                 "status":"PENDING"}"""))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getReservation_returns404ReservationNotFound() throws Exception {
        when(reservationService.getReservationById(999L)).thenThrow(new ReservationNotFoundException(999L));

        mockMvc.perform(get("/api/v1/reservations/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reservation with id 999 not found"));
    }

    @Test
    void patchStatus_returns409OnInvalidTransition() throws Exception {
        when(reservationService.changeStatus(eq(1L), any()))
                .thenThrow(new InvalidStatusTransitionException(
                        ReservationStatus.CANCELLED, ReservationStatus.APPROVED));

        mockMvc.perform(patch("/api/v1/reservations/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}