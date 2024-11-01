package nbc_final.gathering.domain.event.controller;

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
public class EventController {

    private final EventService eventService;

    /**
     * 이벤트 생성
     *
     * @param authUser    인증 사용자
     * @param gatheringId 모임 ID
     * @param requestDto  생성 요청 데이터
     * @return 생성된 이벤트
     */
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
    @GetMapping("/v1/gatherings/{gatheringId}/events")
    public ResponseEntity<ApiResponse<EventListResponseDto>> getAllEvents(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId) {
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
    @GetMapping("/v1/gatherings/{gatheringId}/events/{eventId}/participants")
    public ResponseEntity<ApiResponse<List<ParticipantResponseDto>>> getParticipants(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId) {

        List<ParticipantResponseDto> participants = eventService.getParticipants(authUser.getUserId(), gatheringId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(participants));
    }
}
