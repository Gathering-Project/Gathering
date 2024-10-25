package nbc_final.gathering.domain.userInterest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.Interest.entity.Interest;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserInterestResponseDto {
  private Interest interests;

  // static factory method
  public static UserInterestResponseDto of(Interest interests) {
    return new UserInterestResponseDto(interests);
  }
}
