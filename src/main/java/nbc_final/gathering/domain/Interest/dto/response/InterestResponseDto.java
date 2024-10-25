package nbc_final.gathering.domain.Interest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.user.enums.InterestType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InterestResponseDto {
  private Long id;
  private InterestType interestType;
}
