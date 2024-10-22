package nbc_final.gathering.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.user.enums.MbtiType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserUpdateRequestDto {

    private String location;
    private String nickname;
    private InterestType interestType;
    private MbtiType mbtiType;

}
