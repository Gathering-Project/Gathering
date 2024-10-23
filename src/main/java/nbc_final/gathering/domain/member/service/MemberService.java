package nbc_final.gathering.domain.member.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.member.dto.response.MemberResponseDto;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;

    @Transactional
    public MemberResponseDto requestToJoin(AuthUser authUser, Long gatheringId) {
        User user = findUserById(authUser);

        // Gathering 조회
        Gathering savedGathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));

        // 이미 가입된 멤버인지 확인
        if (memberRepository.existsByUserAndGathering(user, savedGathering)) {
            throw new ResponseCodeException(ResponseCode.ALREADY_REQUESTED);  // ResponseCode 확인해주세요
        }

        // 새로운 멤버 생성
        Member member = new Member(user, savedGathering, MemberRole.USER, MemberStatus.PENDING);
        memberRepository.save(member);

        return new MemberResponseDto(member);
    }

//    @Transactional
//    public MemberResponseDto approveMember(AuthUser authUser, Long memberId) {
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));
//
//        Gathering gathering = member.getGathering();
//
//        User user = userRepository.findById(authUser.getUserId())
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//
//        Member hostMember = memberRepository.findByUserAndGathering(user, gathering)
//                .orElseThrow(() -> new IllegalArgumentException("소모임의 HOST가 아닙니다."));
//
//        if (hostMember.getRole() != MemberRole.HOST) {
//            throw new IllegalArgumentException("HOST 권한이 있는 사용자만 승인할 수 있습니다.");
//        }
//
//        member.approve();
//        return new MemberResponseDto(member);
//    }
//
//    public List<MemberResponseDto> getAllMembers(AuthUser authUser, Long gatheringId) {
//        if (!memberRepository.existsByUserIdAndGatheringId(authUser.getUserId(), gatheringId)) {
//            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
//        }
//
//        List<Member> members = memberRepository.findAllByGatheringId(gatheringId);
//        return members.stream()
//                .map(MemberResponseDto::new)
//                .collect(Collectors.toList());
//    }
//
//    public MemberResponseDto getMemberById(AuthUser authUser, Long gatheringId, Long memberId) {
//        if (!memberRepository.existsByUserIdAndGatheringId(authUser.getUserId(), gatheringId)) {
//            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
//        }
//
//        Member member = memberRepository.findByIdAndGatheringId(memberId, gatheringId)
//                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));
//
//        return new MemberResponseDto(member);
//    }
//
//    @Transactional
//    public Member updateMemberRole(Long memberId, MemberRole role) {
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 존재하지 않습니다."));
//
//        if (member.getRole() != MemberRole.HOST) {
//            throw new IllegalArgumentException("HOST 권한을 가진 사용자만 수정할 수 있습니다.");
//        }
//
//        member.setRole(role);
//        return member;
//    }
//
//    @Transactional
//    public void removeMember(Long memberId) {
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 존재하지 않습니다."));
//
//        if (member.getRole() != MemberRole.HOST) {
//            throw new IllegalArgumentException("HOST 권한을 가진 사용자만 삭제할 수 있습니다.");
//        }
//
//        memberRepository.delete(member);
//    }
//
    private User findUserById(AuthUser authUser) {
        return userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
    }
}
