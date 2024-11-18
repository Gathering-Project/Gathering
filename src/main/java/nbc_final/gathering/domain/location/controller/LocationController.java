package nbc_final.gathering.domain.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Location-Recommend API", description = "사용자 주소 기반 장소 추천 관련 API 입니다.")
public class LocationController {

  private final LocationService locationService;

  /**
   * 주소로부터 위도, 경도를 구하고, 그 위도 경도로 주변 장소 추천.
   *
   * @param recommandRequestDto
   */
  @Operation(summary = "주변 장소 추천", description = "유저의 주소로부터 위도, 경도를 구하고, 해당 위치 데이터로 주변 장소들을 추천합니다.")
  @GetMapping("v1/places/recommend")
  public ResponseEntity<ApiResponse<RecommandResponseDto>> getNearbyPlacesFromAddress(
      @RequestBody RecommandRequestDto recommandRequestDto) {

    RecommandResponseDto res = locationService.getNearbyPlacesFromAddress(recommandRequestDto);
    return ResponseEntity.ok(ApiResponse.createSuccess(res));
  }
}
