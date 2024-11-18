package nbc_final.gathering.domain.gathering.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.gathering.dto.GatheringElasticDto;
import nbc_final.gathering.domain.gathering.dto.request.GatheringRequestDto;
import nbc_final.gathering.domain.gathering.dto.response.GatheringResponseDto;
import nbc_final.gathering.domain.gathering.dto.response.GatheringWithCountResponseDto;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.service.GatheringService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Gathering API", description = "소모임 관련 API 모음입니다.")
public class GatheringController {

    private final GatheringService gatheringService;
    /**
     * 제목/위치를 기준으로 모임을 검색
     *
     * @param title
     * @param location
     * @return
     * @throws ResponseCodeException 'title' 또는 'location'이 모두 제공되지 않은 경우 발생.
     */
    @Operation(summary = "제목/위치 기준 모임 검색 기능", description = "제목과 위치를 기준으로 모임을 검색하는 기능을 제공합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<GatheringElasticDto>>> searchGatherings(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location) {

        List<GatheringElasticDto> gatherings;

        if (title != null && location != null) {
            gatherings = gatheringService.searchGatheringsByTitleAndLocation(title, location);
        } else if (title != null) {
            gatherings = gatheringService.searchGatheringsByTitle(title);
        } else if (location != null) {
            gatherings = gatheringService.searchGatheringsByLocation(location);
        } else {
            throw new ResponseCodeException(ResponseCode.INVALID_SEARCH);
        }

        return ResponseEntity.ok(ApiResponse.createSuccess(gatherings));
    }

    /**
     * 소모임 생성
     *
     * @param authUser
     * @param gatheringRequestDto
     * @return
     */
    @Operation(summary = "소모임 생성(개최)", description = "소모임을 생성(개최)합니다.")
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
    @Operation(summary = "소모임 단건 조회", description = "특정 소모임의 상세 정보에 대해 조회합니다.")
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
    @Operation(summary = "인기 소모임 조회", description = "조회수에 따른 소모임의 인기 랭킹을 조회합니다.")
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
    @Operation(summary = "소모임 다건 조회", description = "현재 존재하는 전체 소모임들의 목록을 조회합니다.")
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
    @Operation(summary = "소모임 정보 수정", description = "소모임의 정보를 수정합니다.")
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
    @Operation(summary = "소모임 삭제(해체)", description = "소모임을 삭제(해체)합니다.")
    @DeleteMapping("/v1/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<Void>> deleteGathering(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable @Valid Long gatheringId) {

        gatheringService.deleteGathering(authUser, gatheringId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    // 소유한 소모임 조회
    @Operation(summary = "소유한 소모임 목록 조회", description = "현재 유저가 주최하고 있는 소모임의 목록을 조회합니다.")
    @GetMapping("/v2/users/owned-gatherings")
    public ResponseEntity<ApiResponse<List<GatheringResponseDto>>> getUserOwnedGatherings(
            @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();
        List<GatheringResponseDto> gatherings = gatheringService.findGatheringsByOwner(userId);
        return ResponseEntity.ok(ApiResponse.createSuccess(gatherings));
    }
}
