package nbc_final.gathering.domain.poll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.poll.entity.Option;

@Getter
@AllArgsConstructor
public class OptionDto {

    private String name;
    private int voteCount;

    public static OptionDto of(Option option) {
        return new OptionDto(
                option.getName(),
                option.getVoteCount()
        );
    }
}

