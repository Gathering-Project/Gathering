package nbc_final.gathering.domain.ad.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.ad.dto.AdDetailsDto;
import nbc_final.gathering.domain.ad.dto.request.AdCreateRequestDto;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.service.AdService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/gatherings/{gatheringId}/ads")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAd(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @RequestBody AdCreateRequestDto requestDto) {

        // 광고 신청을 위한 로직 호출
        Ad ad = adService.createAd(gatheringId, requestDto.getDurationDays(), authUser.getUserId(), requestDto.getStartDate());

        return ResponseEntity.ok(ApiResponse.createSuccess("광고 신청이 완료되었습니다."));
    }

    @GetMapping("/{adId}")
    public ResponseEntity<ApiResponse<AdDetailsDto>> getAd(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long adId) {
        AdDetailsDto adDetails = adService.getAdDetailsById(gatheringId, adId, authUser.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(adDetails));
    }

    @PostMapping("/validate-ad")
    public ResponseEntity<?> validateAd(@RequestParam Long gatheringId,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        adService.validateAdDateRange(gatheringId, startDate, endDate);
        return ResponseEntity.ok().body("Validation successful");
    }
}
