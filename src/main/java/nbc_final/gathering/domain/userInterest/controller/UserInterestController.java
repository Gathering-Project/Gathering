package nbc_final.gathering.domain.userInterest.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.userInterest.dto.request.UserInterestRequestDto;
import nbc_final.gathering.domain.userInterest.dto.response.UserInterestResponseDto;
import nbc_final.gathering.domain.userInterest.service.UserInterestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserInterestController {

  private final UserInterestService userInterestService;

  /**
   * 관심사 추가
   * @param authUser 인증된 사용자 정보
   * @param requestDto 생성 요청 데이터
   * @return 멤버 가입 요청 결과
   */
  @PostMapping("/v1/user/interests/add")
  public ResponseEntity<ApiResponse<UserInterestResponseDto>> addInterest(@AuthenticationPrincipal AuthUser authUser,
                                                                    @RequestBody UserInterestRequestDto requestDto) {

    UserInterestResponseDto response = userInterestService.addUserInterest(authUser, requestDto);
    return ResponseEntity.ok(ApiResponse.createSuccess(response));
  }
}
