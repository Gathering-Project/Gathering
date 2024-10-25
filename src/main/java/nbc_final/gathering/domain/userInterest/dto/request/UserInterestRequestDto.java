package nbc_final.gathering.domain.userInterest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.user.enums.InterestType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInterestRequestDto {

  private InterestType interestType;
}
