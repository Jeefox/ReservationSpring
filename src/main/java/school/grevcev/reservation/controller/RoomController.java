package school.grevcev.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.grevcev.reservation.dto.CreateRoomRequest;
import school.grevcev.reservation.dto.RoomResponse;
import school.grevcev.reservation.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/{id}")
    public RoomResponse getRoom(@PathVariable Long id){
        return roomService.findById(id);
    }

    @GetMapping
    public List<RoomResponse> getRooms(){
        return roomService.findAll();
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request){
        RoomResponse created = roomService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
