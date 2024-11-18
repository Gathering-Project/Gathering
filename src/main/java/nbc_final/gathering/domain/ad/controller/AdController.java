package nbc_final.gathering.domain.ad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.ad.dto.AdDetailsDto;
import nbc_final.gathering.domain.ad.dto.request.AdCreateRequestDto;
import nbc_final.gathering.domain.ad.dto.response.AdCreateResponseDto;
import nbc_final.gathering.domain.ad.dto.response.AdListResponseDto;
import nbc_final.gathering.domain.ad.service.AdService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Advertisement API", description = "광고 관련 API 모음입니다.")
public class AdController {

    private final AdService adService;

    /**
     * 광고 날짜 검증
     *
     * @param gatheringId 모임 ID
     * @param startDate   광고 시작일
     * @param endDate     광고 종료일
     * @return 검증 결과
     */
    @Operation(summary = "광고 날짜 검증", description = "광고 날짜를 검증합니다.")
    @PostMapping("/v2/gatherings/{gatheringId}/ads/validate")
    public ResponseEntity<ApiResponse<Void>> validateAdDates(
            @PathVariable Long gatheringId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        adService.validateAdDateRange(gatheringId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 광고 결제 요청
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @param requestDto  광고 생성 요청 데이터
     * @return 생성된 광고 정보
     */
    @Operation(summary = "광고 결제 요청", description = "광고 결제를 요청합니다.")
    @PostMapping("/v2/gatherings/{gatheringId}/ads/request-payment")
    public ResponseEntity<ApiResponse<AdCreateResponseDto>> requestAdPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @Valid @RequestBody AdCreateRequestDto requestDto) {

        AdCreateResponseDto responseDto = adService.requestAd(authUser.getUserId(), gatheringId, requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    /**
     * 광고 상태 업데이트
     *
     * @param adId 광고 ID
     * @return 상태 업데이트 결과
     */
    @Operation(summary = "광고 상태 업데이트", description = "광고 상태를 업데이트합니다.")
    @PostMapping("/v2/gatherings/{gatheringId}/ads/update-status")
    public ResponseEntity<ApiResponse<Void>> updateAdStatusAfterPayment(
            @RequestParam Long adId) {

        adService.updateAdStatusToReady(adId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 광고 상세 정보 조회
     *
     * @param gatheringId 모임 ID
     * @param adId        광고 ID
     * @return 광고 상세 정보
     */
    @Operation(summary = "광고 상세 정보 조회", description = "광고에 대한 상제 정보를 조회합니다.")
    @GetMapping("/v2/gatherings/{gatheringId}/ads/{adId}")
    public ResponseEntity<ApiResponse<AdDetailsDto>> getAdDetails(
            @PathVariable Long gatheringId,
            @PathVariable Long adId) {

        AdDetailsDto adDetails = adService.getAdDetails(adId);
        return ResponseEntity.ok(ApiResponse.createSuccess(adDetails));
    }

    /**
     * 특정 기간 내 광고 조회
     *
     * @param startDate 광고 시작 기간
     * @param endDate   광고 종료 기간
     * @return 조회된 광고 리스트
     */
    @Operation(summary = "특정 기간 내 광고 조회", description = "특정 기간 내의 광고를 조회합니다.")
    @GetMapping("/v2/ads")
    public ResponseEntity<ApiResponse<AdListResponseDto>> getAdsWithinPeriod(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        AdListResponseDto ads = adService.getAdsWithinPeriod(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.createSuccess(ads));
    }
}
