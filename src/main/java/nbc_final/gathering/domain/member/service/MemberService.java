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

        Member member = new Member(user, savedGathering, MemberRole.GUEST, MemberStatus.PENDING);
        memberRepository.save(member);

        return MemberResponseDto.from(member);
    }

    @Transactional
    public MemberResponseDto approveMember(AuthUser authUser, Long memberId) {

        // 승인할 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

        // 소모임 조회
        Gathering gathering = member.getGathering();

        // 현재 로그인한 사용자가 소모임의 HOST인지 확인
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

        // 현재 소모임의 승인된 멤버 수 확인
        long approvedMembersCount = memberRepository.findAllByGatheringId(gathering.getId())
                .stream()
                .filter(m -> m.getStatus() == MemberStatus.APPROVED)
                .count();

        // 소모임 최대 인원을 초과할 경우
        if (approvedMembersCount >= gathering.getGatheringMaxCount()) {
            // 멤버는 여전히 PENDING 상태로 유지
            throw new ResponseCodeException(ResponseCode.FULL_MEMBER, "소모임의 최대 인원을 초과했습니다.");
        }

        // 인원이 초과되지 않았으면 멤버 승인
        member.approve();

        return MemberResponseDto.from(member);
    }

    public List<MemberResponseDto> getAllMembers(AuthUser authUser, Long gatheringId) {

        // ADMIN인지 확인
        if (!isAdmin(authUser)) {
            // ADMIN이 아닌 경우 현재 사용자의 멤버 정보 가져오기
            User user = userRepository.findById(authUser.getUserId())
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

            Member currentUserMember = memberRepository.findByUserAndGathering(user, gatheringRepository.findById(gatheringId)
                            .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING)))
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN));

            // 현재 사용자의 역할이 HOST 또는 GUEST가 아니면 조회 불가
            if (currentUserMember.getRole() != MemberRole.HOST && currentUserMember.getRole() != MemberRole.GUEST) {
                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }

            // 현재 사용자의 상태가 APPROVED가 아니면 조회 불가
            if (currentUserMember.getStatus() != MemberStatus.APPROVED) {
                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }

            // GUEST는 APPROVED 상태의 멤버만 조회 가능
            if (currentUserMember.getRole() == MemberRole.GUEST) {
                return memberRepository.findAllByGatheringId(gatheringId).stream()
                        .filter(member -> member.getStatus() == MemberStatus.APPROVED) // APPROVED 상태의 멤버만 필터링
                        .map(MemberResponseDto::from)
                        .collect(Collectors.toList());
            }
        }

        // ADMIN 또는 HOST일 경우 모든 상태의 멤버를 조회 가능
        return memberRepository.findAllByGatheringId(gatheringId).stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
    }

    public MemberResponseDto getMemberById(AuthUser authUser, Long gatheringId, Long memberId) {

        // 해당 소모임을 Gathering 객체로 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        // ADMIN인지 확인
        if (!isAdmin(authUser)) {
            // ADMIN이 아니면 현재 사용자의 멤버 정보 가져오기
            User user = userRepository.findById(authUser.getUserId())
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

            Member currentUserMember = memberRepository.findByUserAndGathering(user, gathering)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN));

            // 현재 사용자의 역할이 HOST 또는 GUEST가 아니면 조회 불가
            if (currentUserMember.getRole() != MemberRole.GUEST && currentUserMember.getRole() != MemberRole.HOST) {
                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }

            // 현재 사용자의 상태가 APPROVED가 아니면 조회 불가
            if (currentUserMember.getStatus() != MemberStatus.APPROVED) {
                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }

            // GUEST는 APPROVED 상태의 멤버만 조회 가능
            if (currentUserMember.getRole() == MemberRole.GUEST) {
                Member member = memberRepository.findByIdAndGatheringId(memberId, gatheringId)
                        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

                if (member.getStatus() != MemberStatus.APPROVED) {
                    throw new ResponseCodeException(ResponseCode.FORBIDDEN); // GUEST는 APPROVED 상태의 멤버만 조회 가능
                }

                return MemberResponseDto.from(member);
            }
        }

        // ADMIN 또는 HOST일 경우 모든 상태의 멤버를 조회 가능
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

        // 관리자 권한 확인
        boolean isAdmin = authUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(UserRole.Authority.ADMIN));

        // 관리자는 멤버 상태나 역할에 상관없이 삭제 가능
        if (isAdmin) {
            memberRepository.delete(memberToDelete);
            return;
        }

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