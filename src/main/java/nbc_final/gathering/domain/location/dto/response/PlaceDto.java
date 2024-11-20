package nbc_final.gathering.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDto {
    private String name;        // 장소 이름
    private String address;     // 장소 주소
    private double latitude;    // 장소의 위도
    private double longitude;   // 장소의 경도
    private int radius;  // 장소 검색 반경
    private String placeId;     // 고유 ID
    private String type;        // 장소 유형

    public PlaceDto(String name, String address, double latitude, double longitude, String placeId, String type) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeId = placeId;
        this.type = type;
    }

    public PlaceDto(double latitude, double longitude, int requestRadius, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = requestRadius;
        this.type = type;
    }
}
