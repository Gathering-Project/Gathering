package nbc_final.gathering.domain.gathering.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.gathering.dto.request.GatheringRequestDto;
import nbc_final.gathering.domain.gathering.dto.response.GatheringResponseDto;
import nbc_final.gathering.domain.gathering.dto.response.GatheringWithCountResponseDto;
import nbc_final.gathering.domain.gathering.service.GatheringService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    /**
     * 소모임 생성
     *
     * @param authUser
     * @param gatheringRequestDto
     * @return
     */
    @PostMapping("/v1/gatherings")
    public ResponseEntity<ApiResponse<GatheringResponseDto>> createGroup(@AuthenticationPrincipal AuthUser authUser,
                                                                         @RequestBody @Valid GatheringRequestDto gatheringRequestDto) {

        GatheringResponseDto res = gatheringService.createGroup(authUser, gatheringRequestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 소모임 단건 조회
     *
     * @param authUser
     * @param gatheringId
     * @return
     */
    @GetMapping("/v1/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<GatheringWithCountResponseDto>> getGathering(@AuthenticationPrincipal AuthUser authUser,
                                                                                   @PathVariable @Valid Long gatheringId) {

        GatheringWithCountResponseDto res = gatheringService.getGathering(authUser, gatheringId);

        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 인기 소모임 조회
     *
     * @param authUser
     * @return
     */
    @GetMapping("/v1/gathering/top-view-list")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getTopViewCardList(@AuthenticationPrincipal AuthUser authUser)
    {
        return ResponseEntity.ok(ApiResponse.createSuccess(gatheringService.getTopViewGatheringList()));
    }


    /**
     * 소모임 다건 조회
     *
     * @param authUser
     * @return
     */
    @GetMapping("/v1/gatherings")
    public ResponseEntity<ApiResponse<List<GatheringResponseDto>>> getAllGatherings(
            @AuthenticationPrincipal AuthUser authUser) {

        List<GatheringResponseDto> res = gatheringService.getAllGatherings(authUser);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 소모임 수정
     *
     * @param authUser
     * @param gatheringId
     * @param gatheringRequestDto
     * @return
     */
    @PutMapping("/v1/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<GatheringResponseDto>> updateGathering(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable @Valid Long gatheringId,
            @RequestBody @Valid GatheringRequestDto gatheringRequestDto) {

        GatheringResponseDto res = gatheringService.updateGathering(authUser, gatheringId, gatheringRequestDto);

        return ResponseEntity.ok(
                ApiResponse.createSuccess(res)
        );
    }

    /**
     * 소모임 삭제
     *
     * @param authUser
     * @param gatheringId
     * @return
     */
    @DeleteMapping("/v1/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<Void>> deleteGathering(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable @Valid Long gatheringId) {

        gatheringService.deleteGathering(authUser, gatheringId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

}
