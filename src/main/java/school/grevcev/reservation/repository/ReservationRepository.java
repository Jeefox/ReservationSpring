package school.grevcev.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.grevcev.reservation.ReservationStatus;
import school.grevcev.reservation.model.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    @Query("select r from Reservation r join fetch r.user join fetch r.room")
    List<Reservation> findAllWithAssociations();

    @Query("select r from Reservation r where r.room.id = :roomId and r.startDate<:endDate and :startDate< r.endDate " +
            "and r.status <> :cancelledStatus and (:excludedId is null or r.id <> :excludedId)")
    List<Reservation> findConflictingReservations(@Param("roomId")Long roomId, @Param("startDate") LocalDate startDate,
                                                @Param("endDate")LocalDate endDate,
                                                @Param("cancelledStatus") ReservationStatus cancelledStatus,
                                                @Param("excludedId") Long excludedId);
}
