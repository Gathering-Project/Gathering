package nbc_final.gathering.domain.location.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDto {

  private String address; // 사용자가 입력한 주소
}
