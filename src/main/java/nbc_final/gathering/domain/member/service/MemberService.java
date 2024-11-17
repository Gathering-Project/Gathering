package nbc_final.gathering.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.common.kafka.util.KafkaNotificationUtil;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final KafkaNotificationUtil kafkaNotificationUtil;

    @Transactional
    public MemberResponseDto requestToJoin(AuthUser authUser, Long gatheringId) {

        // 유저 조회
        User user = findUserById(authUser);

        // 소모임 조회
        Gathering savedGathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        // 이미 소모임에 가입한 적이 있는지 확인
        Optional<Member> existingMember = memberRepository.findByUserAndGathering(user, savedGathering);

        // 멤버 상태 확인
        if (existingMember.isPresent()) {
            Member member = existingMember.get();
            // REJECT 상태인 경우
            if (member.getStatus() == MemberStatus.REJECTED) {
                throw new ResponseCodeException(ResponseCode.REJECTED_MEMBER);
            }
            // APPROVED 상태인 경우
            else if (member.getStatus() == MemberStatus.APPROVED) {
                throw new ResponseCodeException(ResponseCode.ALREADY_MEMBER);
            }
            // PENDING 상태인 경우
            else if (member.getStatus() == MemberStatus.PENDING) {
                throw new ResponseCodeException(ResponseCode.ALREADY_REQUESTED);
            }
        }

        // 새로운 가입 요청 처리 (PENDING 상태로 저장)
        Member newMember = new Member(user, savedGathering, MemberRole.GUEST, MemberStatus.PENDING);
        memberRepository.save(newMember);

        kafkaNotificationUtil.notifyGuestMember(newMember.getId(), "게스트 님, 가입 신청이 완료되었습니다.");

        savedGathering.getMembers().stream()
                .filter(m -> m.getRole() == MemberRole.HOST)
                .forEach(hostMember -> kafkaNotificationUtil.notifyHostMember(hostMember.getId(), "호스트 님, 새로운 가입 신청이 들어왔습니다."));

        return MemberResponseDto.from(newMember);
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
            throw new ResponseCodeException(ResponseCode.FULL_MEMBER);
        }

        // 인원이 초과되지 않았으면 멤버 승인
        member.approve();

        // 게스트와 호스트에게 알림 메시지 전송
        String guestMessage = gathering.getTitle() + " 소모임에 가입이 승인되었습니다."; // 소모임 이름과 함께 게스트에게 전송
        kafkaNotificationUtil.notifyGuestMember(memberId, guestMessage);

        String hostMessage = member.getUser().getNickname() + "이(가) " + gathering.getTitle() + " 소모임에 가입했습니다."; // 가입 신청한 멤버의 이름과 소모임 제목을 호스트에게 전송
        kafkaNotificationUtil.notifyHostMember(currentMember.getId(), hostMessage);

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

        // 삭제된 멤버에게 알림 전송
        String gatheringTitle = memberToDelete.getGathering().getTitle();
        String memberName = memberToDelete.getUser().getNickname(); // User 객체의 이름을 가져와서 사용

        kafkaNotificationUtil.notifyGuestMember(memberToDelete.getId(), gatheringTitle + "에서 삭제되었습니다.");

        // 호스트에게 알림 전송
        kafkaNotificationUtil.notifyHostMember(currentUserMember.getId(), memberName + "이(가) 삭제되었습니다.");

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

        kafkaNotificationUtil.notifyGuestMember(memberToReject.getId(), "가입 요청이 거절되었습니다.");

        // 거절된 멤버 정보 반환
        return MemberResponseDto.from(memberToReject);
    }

    @Transactional
    public void notifyAllGuests(AuthUser authUser, Long gatheringId, String message) {

        // 소모임 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING, "소모임이 존재하지 않습니다."));

        // 현재 로그인한 사용자가 소모임의 호스트인지 확인
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER, "사용자가 존재하지 않습니다."));


        // 호스트 권한과 승인 상태 확인
        Member currentMember = memberRepository.findByUserAndGathering(user, gathering)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN, "사용자에게 권한이 없습니다."));

        // 호스트가 아니거나 승인되지 않은 상태일 경우 예외 발생
        if (currentMember.getRole() != MemberRole.HOST || currentMember.getStatus() != MemberStatus.APPROVED) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN, "호스트 권한 또는 승인 상태가 아닙니다.");
        }

        // 소모임 내 승인된 게스트 멤버 조회
        List<Member> guestMembers = memberRepository.findAllByGatheringId(gatheringId).stream()
                .filter(member -> member.getRole() == MemberRole.GUEST && member.getStatus() == MemberStatus.APPROVED)
                .collect(Collectors.toList());

        if (guestMembers.isEmpty()) {
            System.err.println("오류: 승인된 게스트 멤버가 없습니다. 소모임 ID: " + gatheringId);
            return; // 메세지를 보낼 게스트가 없으면 종료
        }

        // 각 게스트 멤버에게 알림 전송
        guestMembers.forEach(guest -> {
            // 게스트에게 메세지 전송
            kafkaNotificationUtil.notifyGuestMember(guest.getId(), message);
            System.out.println("알림 전송 성공: 게스트 ID " + guest.getId() + ", 메시지: " + message);
        });
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