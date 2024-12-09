package nbc_final.gathering.domain.poll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.poll.dto.request.PollCreateRequestDto;
import nbc_final.gathering.domain.poll.dto.response.PollResponseDto;
import nbc_final.gathering.domain.poll.service.PollService;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Poll API", description = "이벤트 내 의사결정 투표 관련 API 모음입니다.")
public class PollController {

    private final PollService pollService;

    /**
     * 이벤트 내 멤버들간의 의사 결정을 위한 투표 생성
     *
     * @param gatheringId
     * @param eventId
     * @param authUser
     * @param requestDto
     * @return 생성된 투표 정보(투표 ID, 소모임 ID, 이벤트 ID, 안건(의제), 선택지 정보 등)
     */
    @Operation(summary = "투표 생성", description = "이벤트 내 의사결정을 위한 멤버들이 참여가능한 투표를 생성합니다.")
    @PostMapping("/v1/gatherings/{gatheringId}/events/{eventId}/polls")
    public ResponseEntity<ApiResponse<PollResponseDto>> createPoll(
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid PollCreateRequestDto requestDto) {
        PollResponseDto res = pollService.createPoll(requestDto, gatheringId, eventId, authUser.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 투표 참여(선택지 투표, 투표 취소, 선택지 변경)
     *
     * @param authUser
     * @param gatheringId
     * @param eventId
     * @param pollId
     * @param selectedOption
     * @return 투표 참여 성공 여부
     */
    @Operation(summary = "투표 참여", description = "선택지 투표, 투표 취소, 선택지 변경 등 이벤트 멤버끼리 자유롭게 투표에 참여할 수 있습니다.")
    @PostMapping("/v1/gatherings/{gatheringId}/events/{eventId}/polls/{pollId}")
    public ResponseEntity<ApiResponse<Void>> castVote(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @PathVariable Long pollId,
            @RequestParam int selectedOption
    ) {
        pollService.castVote(gatheringId, eventId, authUser.getUserId(), pollId, selectedOption);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 투표 단건 조회
     *
     * @param authUser
     * @param gatheringId
     * @param eventId
     * @param pollId
     * @return 단건 투표 정보 반환
     */
    @Operation(summary = "특정 투표 현황 조회", description = "진행 중 혹은 마감된 해당 투표의 정보를 확인할 수 있습니다.")
    @GetMapping("/v1/gatherings/{gatheringId}/events/{eventId}/polls/{pollId}")
    public ResponseEntity<ApiResponse<PollResponseDto>> getPoll(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @PathVariable Long pollId
    ) {
        PollResponseDto res = pollService.getPoll(gatheringId, eventId, authUser.getUserId(), pollId);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 투표 다건 조회
     *
     * @param gatheringId
     * @param eventId
     * @param authUser
     * @param page
     * @param size
     * @return 다건 투표 정보 반환
     */
    @GetMapping("/v1/gatherings/{gatheringId}/events/{eventId}/polls")
    public ResponseEntity<ApiResponse<Page<PollResponseDto>>> getPolls(
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PollResponseDto> res = pollService.getPolls(gatheringId, eventId, authUser.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 투표 마감
     *
     * @param authUser
     * @param gatheringId
     * @param eventId
     * @param pollId
     * @return 투표 마감 성공 여부
     */
    @Operation(summary = "투표 마감", description = "현재 진행 중이던 투표를 마감합니다.")
    @PutMapping("/v1/gatherings/{gatheringId}/events/{eventId}/polls/{pollId}/finish")
    public ResponseEntity<ApiResponse<Void>> finishPoll(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @PathVariable Long pollId
    ) {
        pollService.finishPoll(gatheringId, eventId, pollId, authUser.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 투표 삭제
     *
     * @param authUser
     * @param gatheringId
     * @param eventId
     * @param pollId
     * @return 투표 삭제 성공 여부
     */
    @Operation(summary = "투표 삭제", description = "해당 투표를 삭제합니다.")
    @DeleteMapping("/v1/gatherings/{gatheringId}/events/{eventId}/polls/{pollId}")
    public ResponseEntity<ApiResponse<Void>> deletePoll(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @PathVariable Long pollId
    ) {
        pollService.deletePoll(gatheringId, eventId, pollId, authUser.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }
}
