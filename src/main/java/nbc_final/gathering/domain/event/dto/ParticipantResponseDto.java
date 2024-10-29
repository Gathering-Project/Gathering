package nbc_final.gathering.domain.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.event.entity.Participant;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ParticipantResponseDto {
    private Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime participationDate;

    public static ParticipantResponseDto from(Participant participant) {
        return new ParticipantResponseDto(
                participant.getUser().getId(),
                participant.getCreatedAt()
        );
    }
}
