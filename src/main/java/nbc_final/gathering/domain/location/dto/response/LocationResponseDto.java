package nbc_final.gathering.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDto {

  private Double latitude;  // 위도
  private Double longitude; // 경도
}
