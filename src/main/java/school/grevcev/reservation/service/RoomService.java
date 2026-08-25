package school.grevcev.reservation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.grevcev.reservation.dto.CreateRoomRequest;
import school.grevcev.reservation.dto.RoomResponse;
import school.grevcev.reservation.dto.RoomStatsResponse;
import school.grevcev.reservation.dto.UpdateRoomRequest;
import school.grevcev.reservation.exception.RoomNotFoundException;
import school.grevcev.reservation.model.Room;
import school.grevcev.reservation.repository.RoomRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    @Transactional(readOnly=true)
    public Page<RoomResponse> findAll(Pageable pageable) {
        return roomRepository.findAll(pageable).map(this::toResponse);
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

    @Transactional
    public RoomResponse update(Long id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id).orElseThrow(()-> new RoomNotFoundException(id));
        room.setName(request.name());
        room.setCapacity(request.capacity());
        return toResponse(room);
    }

    @Transactional
    public void delete(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(()-> new RoomNotFoundException(id));
        roomRepository.delete(room);
    }
}
