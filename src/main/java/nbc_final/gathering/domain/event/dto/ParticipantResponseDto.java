package nbc_final.gathering.domain.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.event.entity.Participant;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ParticipantResponseDto {
    private Long userId;
    private LocalDateTime participationDate;

    public static ParticipantResponseDto from(Participant participant) {
        return new ParticipantResponseDto(
                participant.getUser().getId(),
                participant.getCreatedAt()
        );
    }
}
