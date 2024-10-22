package nbc_final.gathering.domain.member.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.member.dto.response.MemberResponseDto;
import nbc_final.gathering.domain.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/gathering/{gatheringId}/request")
    public ResponseEntity<ApiResponse<MemberResponseDto>> requestToJoin(
            @PathVariable Long gatheringId,
            @RequestBody AuthUser authUser) {

        MemberResponseDto response = memberService.requestToJoin(authUser, gatheringId);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

//    @PostMapping("/approve/{memberId}")
//    public ResponseEntity<ApiResponse<MemberResponseDto>> approveMember(
//            @PathVariable Long memberId,
//            @RequestBody AuthUser authUser) {
//
//        MemberResponseDto response = memberService.approveMember(authUser, memberId);
//
//        return ResponseEntity.ok(new ApiResponse<>(200, "가입이 승인되었습니다.", response));
//    }

//        return ResponseEntity.ok(new ApiResponse<>(200, "가입이 승인되었습니다.", response));
    }

//    @GetMapping("/gathering/{gatheringId}")
//    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getAllMembers(
//            @PathVariable Long gatheringId,
//            @RequestBody AuthUser authUser) {
//        List<MemberResponseDto> response = memberService.getAllMembers(authUser, gatheringId);
//        return ResponseEntity.ok(ApiResponse.createSuccess(response));
//    }

//    @GetMapping("/gathering/{gatheringId}/{memberId}")
//    public ResponseEntity<ApiResponse<MemberResponseDto>> getMemberById(
//            @PathVariable Long gatheringId,
//            @PathVariable Long memberId,
//            @RequestBody AuthUser authUser) {
//
//        MemberResponseDto response = memberService.getMemberById(authUser, gatheringId, memberId);
//        return ResponseEntity.ok(ApiResponse.createSuccess(response));
//    }

//    @PutMapping("/{memberId}")
//    public ResponseEntity<MemberResponseDto> updateMemberRole(@PathVariable Long memberId, @RequestBody MemberRoleUpdateRequestDto requestDto) {
//        Member updatedMember = memberService.updateMemberRole(memberId, requestDto.getRole());
//        return ResponseEntity.ok(new MemberResponseDto(updatedMember));
//    }

//    @DeleteMapping("/{memberId}")
//    public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
//        memberService.removeMember(memberId);
//        return ResponseEntity.noContent().build();
//    }
//}
