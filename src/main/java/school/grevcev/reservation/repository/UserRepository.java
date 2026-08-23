package school.grevcev.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.grevcev.reservation.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
