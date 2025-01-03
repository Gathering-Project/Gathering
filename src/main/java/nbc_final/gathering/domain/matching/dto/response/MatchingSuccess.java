package nbc_final.gathering.domain.matching.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchingSuccess {

    private String matchingId;
    private Long userId1;
    private Long userId2;
}
