package nbc_final.gathering.domain.event.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.event.dto.ParticipantResponseDto;
import nbc_final.gathering.domain.event.dto.request.EventCreateRequestDto;
import nbc_final.gathering.domain.event.dto.request.EventUpdateRequestDto;
import nbc_final.gathering.domain.event.dto.response.EventListResponseDto;
import nbc_final.gathering.domain.event.dto.response.EventResponseDto;
import nbc_final.gathering.domain.event.dto.response.EventUpdateResponseDto;
import nbc_final.gathering.domain.event.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "소모임 내의 이벤트(일정) 관련 API 모음입니다.")
public class EventController {

    private final EventService eventService;

    /**
     * 이벤트 검색
     *
     * @param keyword 검색 키워드
     * @return 검색 결과 목록
     */
    @GetMapping("/v1/events/search")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> searchEvents(
            @RequestParam String keyword) {
        List<EventResponseDto> searchResults = eventService.searchEvents(keyword);
        return ResponseEntity.ok(ApiResponse.createSuccess(searchResults));
    }

    /**
     * 이벤트 생성
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @param requestDto  생성 요청 데이터
     * @return 생성된 이벤트
     */
    @Operation(summary = "이벤트 생성", description = "소모임 멤버들끼리 각자 자유롭게 모일 수 있는 이벤트를 생성(개최)합니다.")
    @PostMapping("/v1/gatherings/{gatheringId}/events")
    public ResponseEntity<ApiResponse<EventResponseDto>> createEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @Valid @RequestBody EventCreateRequestDto requestDto) {

        EventResponseDto createdEvent = eventService.createEvent(authUser.getUserId(), gatheringId, requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(createdEvent));
    }

    /**
     * 이벤트 수정
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @param eventId     이벤트 ID
     * @param requestDto  수정 요청 데이터
     * @return 수정된 이벤트
     */
    @Operation(summary = "이벤트 수정", description = "생성된 이벤트의 정보를 수정합니다.")
    @PutMapping("/v1/gatherings/{gatheringId}/events/{eventId}")
    public ResponseEntity<ApiResponse<EventUpdateResponseDto>> updateEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequestDto requestDto) {
        EventUpdateResponseDto updatedEvent = eventService.updateEvent(authUser.getUserId(), gatheringId, eventId, requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(updatedEvent));
    }

    /**
     * 이벤트 목록 조회
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @return 이벤트 목록
     */
    @Operation(summary = "소모임 내 이벤트 목록 조회", description = "해당 소모임 내에 존재하는 이벤트의 목록을 조회합니다.")
    @GetMapping("/v1/gatherings/{gatheringId}/events")
    public ResponseEntity<ApiResponse<EventListResponseDto>> getAllEvents(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId) throws JsonProcessingException {
        EventListResponseDto events = eventService.getAllEvents(authUser.getUserId(), gatheringId);
        return ResponseEntity.ok(ApiResponse.createSuccess(events));
    }

    /**
     * 이벤트 상세 조회
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @param eventId     이벤트 ID
     * @return 이벤트 상세 정보
     */
    @Operation(summary = "이벤트 상세 조회", description = "해당 이벤트의 상세 정보를 조회합니다.")
    @GetMapping("/v1/gatherings/{gatheringId}/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponseDto>> getEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId) {
        EventResponseDto event = eventService.getEvent(authUser.getUserId(), gatheringId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(event));
    }

    /**
     * 이벤트 삭제
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @param eventId     이벤트 ID
     * @return 성공 여부
     */
    @Operation(summary = "이벤트 삭제", description = "해당 이벤트를 삭제합니다.")
    @DeleteMapping("/v1/gatherings/{gatheringId}/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId) {
        eventService.deleteEvent(authUser.getUserId(), gatheringId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }


    /**
     * 이벤트 참가
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @param eventId     이벤트 ID
     * @return 성공 여부
     */
    @Operation(summary = "이벤트 참가", description = "해당 이벤트에 참가 신청합니다.")
    @PostMapping("/v1/gatherings/{gatheringId}/events/{eventId}/participate")
    public ResponseEntity<ApiResponse<Void>> participateInEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId) {
        eventService.joinEventWithLock(authUser.getUserId(), gatheringId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 이벤트 참가 취소
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @param eventId     이벤트 ID
     * @return 성공 여부
     */
    @Operation(summary = "이벤트 참가 취소", description = "해당 이벤트에 참가 신청한 것을 취소합니다.")
    @PostMapping("/v1/gatherings/{gatheringId}/events/{eventId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelParticipation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId) {
        eventService.cancelParticipation(authUser.getUserId(), gatheringId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 이벤트 참가자 조회
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @param eventId     이벤트 ID
     * @return 참가자 목록
     */
    @Operation(summary = "이벤트 참가자 조회", description = "해당 이벤트에 참가 중인 참가자들의 목록을 조회합니다.")
    @GetMapping("/v1/gatherings/{gatheringId}/events/{eventId}/participants")
    public ResponseEntity<ApiResponse<List<ParticipantResponseDto>>> getParticipants(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId) {

        List<ParticipantResponseDto> participants = eventService.getParticipants(authUser.getUserId(), gatheringId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(participants));
    }
}
