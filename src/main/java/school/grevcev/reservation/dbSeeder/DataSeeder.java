package school.grevcev.reservation.dbSeeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import school.grevcev.reservation.model.Room;
import school.grevcev.reservation.model.User;
import school.grevcev.reservation.repository.RoomRepository;
import school.grevcev.reservation.repository.UserRepository;

//@Profile("dev")
@Component
public class DataSeeder implements CommandLineRunner {
    private UserRepository userRepository;
    private RoomRepository roomRepository;

    public DataSeeder(UserRepository userRepository, RoomRepository roomRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }
    @Override
    public void run(String... args) throws Exception {
        User ivan = User.builder()
                .name("Ivan")
                .email("ivan@email.com")
                .build();
        Room luxuryRoom = Room.builder()
                .name("luxury")
                .capacity(2)
                .build();
        User maria = User.builder()
                .name("Maria")
                .email("maria@email.com")
                .build();
        Room casualRoom = Room.builder()
                .name("casual")
                .capacity(4)
                .build();

        userRepository.save(ivan);
        userRepository.save(maria);

        roomRepository.save(luxuryRoom);
        roomRepository.save(casualRoom);
    }
}
