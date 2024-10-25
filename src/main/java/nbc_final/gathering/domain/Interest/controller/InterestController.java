package nbc_final.gathering.domain.Interest.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.Interest.dto.response.InterestResponseDto;
import nbc_final.gathering.domain.Interest.service.InterestService;
import nbc_final.gathering.domain.user.enums.InterestType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InterestController {

  private final InterestService interestService;

  /**
   * 모든 관심사 목록 조회
   * @return 관심사 목록
   */
  @GetMapping("/v1/interests")
  public ResponseEntity<ApiResponse<List<InterestResponseDto>>> getAllInterests(@AuthenticationPrincipal AuthUser authUser) {
    List<InterestResponseDto> reponses = interestService.getAllInterests(authUser);
    return ResponseEntity.ok(ApiResponse.createSuccess(reponses));
  }

  /**
   * 관심사 추가
   * @param interestType 추가할 관심사 타입
   * @return 추가된 관심사 정보
   */
  @PostMapping("/v1/interests")
  public ResponseEntity<ApiResponse<InterestResponseDto>> addInterest(@RequestBody InterestType interestType) {
    InterestResponseDto reponse = interestService.addInterest(interestType);
    return ResponseEntity.ok(ApiResponse.createSuccess(reponse));
  }
}
