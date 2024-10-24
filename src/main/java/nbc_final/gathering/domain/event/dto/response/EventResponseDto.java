package nbc_final.gathering.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.comment.dto.response.CommentResponseDto;
import nbc_final.gathering.domain.event.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

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
                null  // 댓글 리스트 없이 반환
        );
    }

    // 댓글이 포함된 메서드
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
}
