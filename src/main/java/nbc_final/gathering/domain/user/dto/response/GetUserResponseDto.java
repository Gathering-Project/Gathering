package nbc_final.gathering.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.user.enums.MbtiType;

@Getter
@AllArgsConstructor
public class GetUserResponseDto {

    private Long userId;
    private String location;
    private String nickname;
    private String email;
    private InterestType interestType;
    private MbtiType mbtiType;

    public static GetUserResponseDto of(User user) {
        return new GetUserResponseDto(
                user.getId(),
                user.getLocation(),
                user.getNickname(),
                user.getEmail(),
                user.getInterestType(),
                user.getMbtiType()
        );
    }

}
