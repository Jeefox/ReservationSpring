package school.grevcev.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.grevcev.reservation.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
