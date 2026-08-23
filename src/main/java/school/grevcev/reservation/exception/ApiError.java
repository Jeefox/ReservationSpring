package school.grevcev.reservation.exception;

import java.util.*;
import java.time.LocalDateTime;

public record ApiError(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> details
) {
}
