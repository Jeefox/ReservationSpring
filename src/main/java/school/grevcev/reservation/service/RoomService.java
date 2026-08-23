package school.grevcev.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.grevcev.reservation.dto.CreateRoomRequest;
import school.grevcev.reservation.dto.RoomResponse;
import school.grevcev.reservation.exception.RoomNotFoundException;
import school.grevcev.reservation.model.Room;
import school.grevcev.reservation.repository.RoomRepository;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    @Transactional(readOnly=true)
    public List<RoomResponse> findAll() {
        return roomRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly=true)
    public RoomResponse findById(Long id) {
        return toResponse(roomRepository.findById(id).orElseThrow(()-> new RoomNotFoundException(id)));
    }

    @Transactional
    public RoomResponse save(CreateRoomRequest request) {
        Room room = Room.builder()
                .name(request.name())
                .capacity(request.capacity())
                .build();
        return toResponse(roomRepository.save(room));
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity());
    }
}
