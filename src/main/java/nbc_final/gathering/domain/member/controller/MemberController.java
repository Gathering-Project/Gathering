package nbc_final.gathering.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.member.dto.MemberElasticDto;
import nbc_final.gathering.domain.member.dto.request.MessageRequestDto;
import nbc_final.gathering.domain.member.dto.response.MemberResponseDto;
import nbc_final.gathering.domain.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Member API", description = "소모임 멤버 관련 API 모음입니다.")
public class MemberController {

    private final MemberService memberService;

    /**
     * 소모임 기반 멤버 검색
     * @param gatheringId
     * @return
     */
//    @GetMapping("/v1/members/gathering/{gatheringId}/search")
//    public ResponseEntity<ApiResponse<List<MemberElasticDto>>> searchMembersByGathering(
//            @PathVariable Long gatheringId) {
//        List<MemberElasticDto> members = memberService.searchMembersByGathering(gatheringId);
//        return ResponseEntity.ok(ApiResponse.createSuccess(members));
//    }

    /**
     * 멤버 가입 신청
     * @param gatheringId 가입을 요청할 소모임의 ID
     * @param authUser 인증된 사용자 정보
     * @return 멤버 가입 요청 결과
     */
    @Operation(summary = "소모임 멤버 가입 신청", description = "특정 소모임에 멤버로 참가 신청합니다.")
    @PostMapping("/v1/members/gathering/{gatheringId}/request")
    public ResponseEntity<ApiResponse<MemberResponseDto>> requestToJoin(
            @PathVariable Long gatheringId,
            @AuthenticationPrincipal AuthUser authUser) {

        MemberResponseDto response = memberService.requestToJoin(authUser, gatheringId);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

    /**
     * 멤버 가입 거절
     * @param memberId 거절할 멤버의 ID
     * @param authUser 인증된 사용자 정보
     * @return 거절된 멤버 정보
     */
    @Operation(summary = "멤버 가입 신청 거절", description = "소모임 주최자가 해당 유저의 소모임 참가 신청을 거절합니다.")
    @PostMapping("/v1/members/{memberId}/reject")
    public ResponseEntity<ApiResponse<MemberResponseDto>> rejectMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal AuthUser authUser) {

        MemberResponseDto response = memberService.rejectMember(authUser, memberId);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

    /**
     * 멤버 가입 승인
     * @param memberId 승인할 멤버의 ID
     * @param authUser 인증된 사용자 정보
     * @return 멤버 승인 결과
     */
    @Operation(summary = "멤버 가입 신청 승인", description = "소모임 주최자가 해당 유저의 소모임 참가 신청을 승인합니다.")
    @PostMapping("/v1/members/{memberId}/approve")
    public ResponseEntity<ApiResponse<MemberResponseDto>> approveMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal AuthUser authUser) {

        MemberResponseDto response = memberService.approveMember(authUser, memberId);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

    /**
     * 소모임의 모든 멤버 조회
     * @param gatheringId 조회할 소모임의 ID
     * @param authUser 인증된 사용자 정보
     * @return 소모임의 모든 멤버 리스트
     */
    @Operation(summary = "모든 멤버 조회", description = "특정 소모임의 모든 멤버의 목록을 조회합니다.")
    @GetMapping("/v1/members/gathering/{gatheringId}")
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getAllMembers(
            @PathVariable Long gatheringId,
            @AuthenticationPrincipal AuthUser authUser) {

        List<MemberResponseDto> response = memberService.getAllMembers(authUser, gatheringId);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

    /**
     * 특정 멤버 정보 조회
     * @param gatheringId 조회할 소모임의 ID
     * @param memberId 조회할 멤버의 ID
     * @param authUser 인증된 사용자 정보
     * @return 특정 멤버의 정보
     */
    @Operation(summary = "특정 멤버 정보 조회", description = "소모임 내 특정 멤버의 정보를 상세 조회합니다.")
    @GetMapping("/v1/members/{memberId}/gathering/{gatheringId}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMemberById(
            @PathVariable Long gatheringId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal AuthUser authUser) {

        MemberResponseDto response = memberService.getMemberById(authUser, gatheringId, memberId);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

    /**
     * 멤버 삭제
     * @param memberId 삭제할 멤버의 ID
     * @param authUser 인증된 사용자 정보
     * @return 삭제 처리 응답
     */
    @Operation(summary = "멤버 강퇴", description = "특정 멤버를 소모임에서 강퇴합니다.")
    @DeleteMapping("/v1/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal AuthUser authUser) {
        memberService.removeMember(authUser, memberId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 소모임 호스트가 게스트 멤버들에게 전체 알림 전송
     * @param gatheringId 알림을 전송할 소모임의 ID
     * @param request 메시지 내용을 담은 요청 DTO
     * @param authUser 인증된 사용자 정보
     * @return 성공적으로 전송 시 성공 응답 반환
     */
    @Operation(summary = "소모임 멤버 전체 알림 전송", description = "소모임 주최자가 소모임 내 모든 멤버에게 공지사항 등의 알림을 전송합니다.")
    @PostMapping("/v1/members/gathering/{gatheringId}/notify")
    public ResponseEntity<ApiResponse<Void>> notifyGuests(
            @PathVariable Long gatheringId,
            @RequestBody MessageRequestDto request,
            @AuthenticationPrincipal AuthUser authUser) {

        // 서비스 메서드 호출을 통해 알림 전송
        memberService.notifyAllGuests(authUser, gatheringId, request.getText());

        // 성공 응답 반환
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }
}
