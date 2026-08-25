package school.grevcev.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import school.grevcev.reservation.ReservationStatus;
import school.grevcev.reservation.dto.*;
import school.grevcev.reservation.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/reservations")
@Tag(name = "Reservations", description = "Управление бронями комнат: создание, изменение, отмена, поиск с фильтрами")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(
            summary = "Создать бронь",
            description = """
            Создаёт новое бронирование для указанного пользователя на указанную комнату.
            Проверяет пересечение дат с активными бронями той же комнаты — при конфликте возвращает 409.
            При успешном создании публикует событие ReservationCreatedEvent.
            """
    )
    @ApiResponse(responseCode = "201", description = "Бронь создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации (неверные даты, отсутствующие поля)")
    @ApiResponse(responseCode = "404", description = "Пользователь или комната не найдены")
    @ApiResponse(responseCode = "409", description = "Комната уже забронирована на указанные даты")
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest createReservationRequest) {
        ReservationResponse created = reservationService.createReservation(createReservationRequest);
        return ResponseEntity
                .created(buildLocation(created.id()))
                .body(created);
    }

    private URI buildLocation(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()   // /api/v1/users
                .path("/{id}")                                     // + /5
                .buildAndExpand(id)
                .toUri();
    }

    @Operation(
            summary = "Получить бронь по ID",
            description = "Возвращает бронирование вместе с данными пользователя и комнаты."
    )
    @ApiResponse(responseCode = "200", description = "Бронь найдена")
    @ApiResponse(responseCode = "404", description = "Бронь не найдена")
    @GetMapping("/{id}")
    public ReservationResponse getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @Operation(
            summary = "Обновить бронь",
            description = """
            Полностью заменяет данные бронирования.
            При смене дат/комнаты выполняется повторная проверка конфликтов,
            исключая саму обновляемую бронь (excludedId).
            """
    )
    @ApiResponse(responseCode = "200", description = "Бронь обновлена")
    @ApiResponse(responseCode = "404", description = "Бронь, пользователь или комната не найдены")
    @ApiResponse(responseCode = "409", description = "Новые даты пересекаются с активной бронью комнаты")
    @PutMapping("/{id}")
    public ReservationResponse updateReservation(@PathVariable Long id, @Valid @RequestBody UpdateReservationRequest reservation) {
        return reservationService.updateReservation(id, reservation);
    }

    @Operation(
            summary = "Удалить бронь",
            description = "Физически удаляет бронирование из базы данных."
    )
    @ApiResponse(responseCode = "204", description = "Бронь удалена")
    @ApiResponse(responseCode = "404", description = "Бронь не найдена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationById(@PathVariable Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Список броней с фильтрацией, пагинацией и сортировкой",
            description = """
            Возвращает страницу бронирований. Все фильтры опциональны и комбинируются через AND.
            Сортировка задаётся параметром sort (например, sort=startDate,desc).
            Нумерация страниц начинается с 0.
            """
    )
    @ApiResponse(responseCode = "200", description = "Страница бронирований")
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

    @Operation(
            summary = "Изменить статус брони",
            description = """
            Выполняет переход состояния по правилам конечного автомата:
            - PENDING → APPROVED или CANCELLED
            - APPROVED → CANCELLED
            - CANCELLED — терминальное состояние (выходов нет)
            
            При невалидном переходе возвращает 409. При успешном изменении публикует
            событие ReservationStatusChangedEvent после коммита транзакции.
            """
    )
    @ApiResponse(responseCode = "200", description = "Статус изменён")
    @ApiResponse(responseCode = "400", description = "Некорректное значение статуса")
    @ApiResponse(responseCode = "404", description = "Бронь не найдена")
    @ApiResponse(responseCode = "409", description = "Переход состояния запрещён правилами FSM")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        ReservationResponse response = reservationService.changeStatus(id, request);
        return ResponseEntity.status(200).body(response);
    }
}
