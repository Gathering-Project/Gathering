package nbc_final.gathering.domain.event.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.comment.dto.response.CommentResponseDto;
import nbc_final.gathering.domain.event.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventResponseDto {

    private Long eventId;
    private Long userId;
    private String title;
    private String description;
    private String date;
    private String location;
    private Integer maxParticipants;
    private long currentParticipants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponseDto> comments;

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
                event.getCreatedAt(),
                event.getUpdatedAt(),
                null
        );
    }

    public static EventResponseDto of(Event event, Long userId, List<CommentResponseDto> comments) {
        return new EventResponseDto(
                event.getId(),
                userId,
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getMaxParticipants(),
                event.getCurrentParticipants(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                comments
        );
    }

    // Redis 참가자 수 포함 생성자
    public static EventResponseDto of(Event event, Long userId, long currentParticipantsCount) {
        return new EventResponseDto(
                event.getId(),
                userId,
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getMaxParticipants(),
                currentParticipantsCount,  // Redis에서 가져온 참가자 수 할당
                event.getCreatedAt(),
                event.getUpdatedAt(),
                null
        );
    }

    public static EventResponseDto of(Event event, Long userId, List<CommentResponseDto> comments, long currentParticipantsCount) {
        return new EventResponseDto(
                event.getId(),
                userId,
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getMaxParticipants(),
                currentParticipantsCount,  // Redis에서 가져온 참가자 수 할당
                event.getCreatedAt(),
                event.getUpdatedAt(),
                comments
        );
    }


}
