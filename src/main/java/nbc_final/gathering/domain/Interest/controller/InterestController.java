package nbc_final.gathering.domain.Interest.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.Interest.dto.response.InterestResponseDto;
import nbc_final.gathering.domain.Interest.service.InterestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InterestController {

  private final InterestService interestService;

  /**
   * 모든 관심사 목록 조회
   *
   * @return 관심사 목록
   */
  @GetMapping("/v1/interests")
  public ResponseEntity<ApiResponse<List<InterestResponseDto>>> getAllInterests(@AuthenticationPrincipal AuthUser authUser) {
    List<InterestResponseDto> reponses = interestService.getAllInterests(authUser);
    return ResponseEntity.ok(ApiResponse.createSuccess(reponses));
  }
//
//  /**
//   * 관심사 추가
//   * @param interestRequestDto 추가할 관심사
//   * @return 추가된 관심사 정보
//   */
//  @PostMapping("/v1/interests")
//  public ResponseEntity<ApiResponse<InterestResponseDto>> addInterest(@RequestBody InterestRequestDto interestRequestDto) {
//    InterestResponseDto reponse = interestService.addInterest(interestRequestDto);
//    return ResponseEntity.ok(ApiResponse.createSuccess(reponse));
//  }
}
