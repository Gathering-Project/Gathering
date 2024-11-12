package nbc_final.gathering.domain.location.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.gathering.dto.response.GatheringResponseDto;
import nbc_final.gathering.domain.location.service.LocationService;
import nbc_final.gathering.domain.location.dto.response.LocationResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LocationController {

  private final LocationService locationService;

  /**
   * 제목/위치를 기준으로 모임을 검색
   *
   * @param address
   * @return
   * @throws ResponseCodeException 'title' 또는 'location'이 모두 제공되지 않은 경우 발생.
   */
  // 주소로부터 위도, 경도를 가져오는 API
  @GetMapping("/location")
  public ResponseEntity<ApiResponse<LocationResponseDto>> getCoordinates(@RequestParam String address) {
    // 서비스 호출하여 주소에서 위도, 경도 정보 추출
    LocationResponseDto res = locationService.getCoordinatesFromAddress(address);
    return ResponseEntity.ok(ApiResponse.createSuccess(res));
  }
}
