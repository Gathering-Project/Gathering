package nbc_final.gathering.domain.location.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.location.dto.request.RecommandRequestDto;
import nbc_final.gathering.domain.location.dto.response.RecommandResponseDto;
import nbc_final.gathering.domain.location.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LocationController {

  private final LocationService locationService;

  /**
   * 주소로부터 위도, 경도를 구하고, 그 위도 경도로 주변 장소 추천.
   *
   * @param recommandRequestDto
   */
  @GetMapping("v1/places/recommend")
  public ResponseEntity<ApiResponse<RecommandResponseDto>> getNearbyPlacesFromAddress(
      @RequestBody RecommandRequestDto recommandRequestDto) {

    RecommandResponseDto res = locationService.getNearbyPlacesFromAddress(recommandRequestDto);
    return ResponseEntity.ok(ApiResponse.createSuccess(res));
  }
}
