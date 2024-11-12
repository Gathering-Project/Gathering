package nbc_final.gathering.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaceDto {
  private String name;        // 장소 이름
  private String address;     // 장소 주소
  private double latitude;    // 장소의 위도
  private double longitude;   // 장소의 경도
  private String placeId;     // 고유 ID
  private String type;        // 장소 유형
}
