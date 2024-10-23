package nbc_final.gathering.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.event.entity.Event;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EventResponseDto {

    private Long eventId;
    private Long userId;
    private String title;
    private String description;
    private String date;
    private String location;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EventResponseDto of(Event event, Long userId) {
        return new EventResponseDto(
                event.getId(),
                userId,
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getMaxParticipants(),
                event.getCurrentParticipants(),
                event.getUpdatedAt(),
                event.getCreatedAt()
        );
    }
}
