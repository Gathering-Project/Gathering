package nbc_final.gathering.domain.poll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.poll.entity.Poll;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PollGetResponseDto {

    private Long pollId;
    private Long gatheringId;
    private Long eventId;
    private String agenda;
    //    private List<Option> options;
    private List<OptionDto> options;


    public static PollGetResponseDto of(Poll poll) {
        return new PollGetResponseDto(
                poll.getId(),
                poll.getGathering().getId(),
                poll.getEvent().getId(),
                poll.getAgenda(),
//                poll.getOptions()
                poll.getOptions().stream()
                        .map(OptionDto::of)
                        .collect(Collectors.toList())
        );
    }
    //
    /* 지금처럼 @JsonIgnore 로 순환참조 끊거나 Json을 DTO로 변환하여 반환*/
//    @Getter
//    public static class OptionDTO {
//        private String name;
//        private int voteCount;
//
//        public OptionDTO(Option option) {
//            this.name = option.getName();
//            this.voteCount = option.getVoteCount();
//        }
//    }

}
