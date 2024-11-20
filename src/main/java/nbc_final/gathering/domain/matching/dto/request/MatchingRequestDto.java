package nbc_final.gathering.domain.matching.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.user.enums.InterestType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchingRequestDto {

    private Long userId;
    private InterestType interestType;
    private String location;

}
