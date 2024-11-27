package nbc_final.gathering.domain.poll.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.poll.entity.Option;
import nbc_final.gathering.domain.poll.entity.Poll;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PollResponseDto {

    private Long pollId;
    private Long gatheringId;
    private Long eventId;
    private String agenda;
    private List<Option> options;

    public static PollResponseDto of(Poll poll) {
        return new PollResponseDto(
                poll.getId(),
                poll.getGathering().getId(),
                poll.getEvent().getId(),
                poll.getAgenda(),
                poll.getOptions()
        );
    }
}

