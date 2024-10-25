package nbc_final.gathering.domain.userInterest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.Interest.entity.Interest;
import nbc_final.gathering.domain.user.enums.InterestType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInterestResponseDto {

  private InterestType interestType;

  public static UserInterestResponseDto of(Interest interest) {
    return new UserInterestResponseDto(
        interest.getInterestType()

    );
  }
}
