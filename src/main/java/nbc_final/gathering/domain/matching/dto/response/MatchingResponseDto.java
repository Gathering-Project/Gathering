package nbc_final.gathering.domain.matching.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.matching.enums.MatchingStatus;
import nbc_final.gathering.domain.user.enums.InterestType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MatchingResponseDto {
    private Long userId1;
    private Long userId2;
}