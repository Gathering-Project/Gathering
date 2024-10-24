package nbc_final.gathering.domain.member.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.member.dto.response.MemberResponseDto;
import nbc_final.gathering.domain.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    /**
     * 멤버 가입 신청
     * @param gatheringId 가입을 요청할 소모임의 ID
     * @param authUser 인증된 사용자 정보
     * @return 멤버 가입 요청 결과
     */
    @PostMapping("/gathering/{gatheringId}/request")
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
    @PostMapping("/{memberId}/reject")
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
    @PostMapping("/{memberId}/approve")
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
    @GetMapping("/gathering/{gatheringId}")
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
    @GetMapping("/{memberId}/gathering/{gatheringId}")
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
    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal AuthUser authUser) {
        memberService.removeMember(authUser, memberId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }
}
