package nbc_final.gathering.domain.member.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.config.JwtUtil;
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
import nbc_final.gathering.domain.user.enums.UserRole;
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
    private final JwtUtil jwtUtil;

    @Transactional
    public MemberResponseDto requestToJoin(AuthUser authUser, Long gatheringId) {

        User user = findUserById(authUser);

        Gathering savedGathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        if (memberRepository.existsByUserAndGathering(user, savedGathering)) {
            throw new ResponseCodeException(ResponseCode.ALREADY_REQUESTED);
        }

        Member member = new Member(user, savedGathering, MemberRole.USER, MemberStatus.PENDING);
        memberRepository.save(member);

        return MemberResponseDto.from(member);
    }

    @Transactional
    public MemberResponseDto approveMember(AuthUser authUser, Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

        Gathering gathering = member.getGathering();

        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        Member currentMember = memberRepository.findByUserAndGathering(user, gathering)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

        if (currentMember.getRole() != MemberRole.HOST) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        if (currentMember.getStatus() != MemberStatus.APPROVED) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        if (member.getStatus() != MemberStatus.PENDING) {
            throw new ResponseCodeException(ResponseCode.ALREADY_REQUESTED);
        }

        member.approve();

        return MemberResponseDto.from(member);
    }

    public List<MemberResponseDto> getAllMembers(AuthUser authUser, Long gatheringId) {

        // 현재 사용자가 해당 소모임에 존재하는지 확인
        if (!memberRepository.existsByUserIdAndGatheringId(authUser.getUserId(), gatheringId)) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        // 현재 사용자의 멤버 정보 가져오기
        Member currentUserMember = memberRepository.findByUserAndGathering(
                        userRepository.findById(authUser.getUserId())
                                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER)),
                        gatheringRepository.findById(gatheringId)
                                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING)))
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN));

        // 사용자의 역할이 HOST 또는 USER가 아니면 조회 불가능
        if (currentUserMember.getRole() != MemberRole.HOST && currentUserMember.getRole() != MemberRole.USER) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        // 사용자의 상태가 APPROVED가 아니면 조회 불가능
        if (currentUserMember.getStatus() != MemberStatus.APPROVED) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        // 소모임에 속한 모든 멤버 조회 (상태 상관없이 조회)
        List<Member> members = memberRepository.findAllByGatheringId(gatheringId);

        // 모든 멤버를 DTO로 변환하여 반환
        return members.stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
    }

    public MemberResponseDto getMemberById(AuthUser authUser, Long gatheringId, Long memberId) {

        // 해당 소모임을 Gathering 객체로 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        // 해당 소모임에 대해 현재 사용자의 권한을 확인
        if (!memberRepository.existsByUserIdAndGatheringId(authUser.getUserId(), gatheringId)) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        // 멤버의 권한 및 상태를 먼저 확인
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        // 사용자가 해당 소모임에 속해 있는지 확인 (Gathering 객체로 조회)
        Member currentUserMember = memberRepository.findByUserAndGathering(user, gathering)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN));

        // 현재 사용자의 역할이 USER나 HOST가 아니면 조회 불가
        if (currentUserMember.getRole() != MemberRole.USER && currentUserMember.getRole() != MemberRole.HOST) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        // 현재 사용자의 상태가 APPROVED가 아니면 조회 불가
        if (currentUserMember.getStatus() != MemberStatus.APPROVED) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        // 권한 및 상태 검증을 통과한 후에 멤버 정보 조회
        Member member = memberRepository.findByIdAndGatheringId(memberId, gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

        return MemberResponseDto.from(member);
    }

    @Transactional
    public void removeMember(AuthUser authUser, Long memberId) {

        // 삭제할 멤버 조회
        Member memberToDelete = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

        // 현재 로그인한 사용자 정보 조회
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        // 소모임 내 현재 사용자의 Member 정보를 가져옴 (현재 사용자의 역할 및 상태를 확인하기 위해)
        Member currentUserMember = memberRepository.findByUserAndGathering(user, memberToDelete.getGathering())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

        // 현재 사용자가 HOST이고 상태가 APPROVED인지 확인
        if (currentUserMember.getRole() != MemberRole.HOST || currentUserMember.getStatus() != MemberStatus.APPROVED) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN); // 권한이 없을 경우 예외 발생
        }

        // 삭제하려는 멤버는 PENDING 상태에서도 삭제 가능하므로 멤버의 상태는 체크하지 않음
        // 멤버 삭제 진행
        memberRepository.delete(memberToDelete);
    }

    @Transactional
    public MemberResponseDto rejectMember(AuthUser authUser, Long memberId) {

        // 거절하려는 멤버 찾기
        Member memberToReject = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

        // 멤버의 상태가 PENDING이어야 함
        if (memberToReject.getStatus() != MemberStatus.PENDING) {
            throw new ResponseCodeException(ResponseCode.ALREADY_MEMBER); // PENDING 상태가 아니라면 거절 불가
        }

        // 현재 로그인한 사용자를 가져오기
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        // 로그인한 사용자가 속한 소모임의 멤버 정보 가져오기
        Gathering gathering = memberToReject.getGathering();
        Member currentUserMember = memberRepository.findByUserAndGathering(user, gathering)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

        // 현재 사용자의 역할이 HOST인지 확인
        if (currentUserMember.getRole() != MemberRole.HOST) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN); // HOST가 아닌 경우 거절 불가
        }

        // 현재 사용자의 상태가 APPROVED인지 확인
        if (currentUserMember.getStatus() != MemberStatus.APPROVED) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN); // APPROVED 상태가 아니면 거절 불가
        }

        // 멤버 거절 처리 (상태를 REJECTED로 변경)
        memberToReject.reject();

        // 거절된 멤버 정보 반환
        return MemberResponseDto.from(memberToReject);
    }

    //----------------- extracted method ------------- //

    private boolean isAdmin(AuthUser authUser) {
        return authUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(UserRole.Authority.ADMIN));
    }

    private User findUserById(AuthUser authUser) {
        return userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
    }
}