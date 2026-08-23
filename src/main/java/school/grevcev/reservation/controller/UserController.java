package school.grevcev.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.grevcev.reservation.dto.CreateUserRequest;
import school.grevcev.reservation.dto.UserResponse;
import school.grevcev.reservation.service.UserService;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id){
        return userService.findById(id);
    }

    @GetMapping
    public List<UserResponse> getUsers(){
        return userService.findAll();
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request){
        UserResponse created = userService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
