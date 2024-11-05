package nbc_final.gathering.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.event.entity.Event;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateResponseDto {

    private Long eventId;
    private String title;
    private String description;
    private String date;
    private String location;
    private Integer maxParticipants;
    private Long currentParticipants;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    public static EventUpdateResponseDto of(Event event, long currentParticipantsCount) {
        return new EventUpdateResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getMaxParticipants(),
                currentParticipantsCount,
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

}
