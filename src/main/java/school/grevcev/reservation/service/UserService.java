package school.grevcev.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.grevcev.reservation.dto.CreateUserRequest;
import school.grevcev.reservation.dto.UserResponse;
import school.grevcev.reservation.exception.UserNotFoundException;
import school.grevcev.reservation.model.User;
import school.grevcev.reservation.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly=true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly=true)
    public UserResponse findById(Long id) {
        return toResponse(userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(id)));
    }

    @Transactional
    public UserResponse save(CreateUserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .build();
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User user){
        return new UserResponse(user.getId(),  user.getName(), user.getEmail());
    }
}
