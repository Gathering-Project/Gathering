package nbc_final.gathering.domain.event.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private Long eventId;
    private Long userId;
    private String title;
    private String description;
    private String date;
    private String location;
    private Integer maxParticipants;
    private long currentParticipants;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;

    private List<CommentResponseDto> comments;

    @JsonCreator
    public EventResponseDto(
            @JsonProperty("eventId") Long eventId,
            @JsonProperty("userId") Long userId,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("date") String date,
            @JsonProperty("location") String location,
            @JsonProperty("maxParticipants") Integer maxParticipants,
            @JsonProperty("currentParticipants") Integer currentParticipants,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt,
            @JsonProperty("comments") List<CommentResponseDto> comments) {
        this.eventId = eventId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.comments = comments;
    }

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
                currentParticipantsCount,
                event.getCreatedAt(),
                event.getUpdatedAt(),
                comments
        );
    }


}
