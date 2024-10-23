package nbc_final.gathering.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.event.entity.Event;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EventUpdateResponseDto {

    private Long eventId;
    private String title;
    private String description;
    private String date;
    private String location;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    public static EventUpdateResponseDto of(Event event) {
        return new EventUpdateResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getMaxParticipants(),
                event.getCurrentParticipants(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
