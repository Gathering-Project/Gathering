package nbc_final.gathering.domain.gathering.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.gathering.dto.request.GatheringRequestDto;
import nbc_final.gathering.domain.gathering.dto.response.GatheringResponseDto;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatheringService {

  private final GatheringRepository gatheringRepository;
  private final UserRepository userRepository;
  private final MemberRepository memberRepository;

  // 그룹 생성 로직
  @Transactional
  public GatheringResponseDto createGroup(AuthUser authUser, GatheringRequestDto gatheringRequestDto) {
    // 유저 조회
    User user = findUserById(authUser);

    // 그룹 생성
    Gathering savedGathering = new Gathering(user.getId(),gatheringRequestDto.getTitle(),
        gatheringRequestDto.getDescription(),
        1, gatheringRequestDto.getGatheringMaxCount(),
        BigDecimal.valueOf(50), gatheringRequestDto.getLocation()
    );

    // 주최자 추가
    Member member = new Member(user, savedGathering, MemberRole.HOST, MemberStatus.APPROVED);
    savedGathering.getMembers().add(member);

    // 그룹 저장
    gatheringRepository.save(savedGathering);
    memberRepository.save(member);

    return GatheringResponseDto.of(savedGathering);
  }

  // 소모임 단 건 조회 로직
  public GatheringResponseDto getGathering(AuthUser authUser, Long gatheringId) {

    List<Member> members = findMembersByUserId(authUser);

    // 유저가 조회하고자 하는 ID 소모임 조회
    for (Member member : members) {
      Gathering gathering = member.getGathering();
      // 소모임을 찾았는데, 그 소모임이 request와 같다면
      if (gathering != null && gathering.getId().equals(gatheringId)) {
        if ((member.getStatus() == MemberStatus.APPROVED)) {
          return GatheringResponseDto.of(gathering);
        }
      }
    }
    throw new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING);
  }

  // 유저가 가입한 소모임 다 건 조회 로직
  public List<GatheringResponseDto> getAllGatherings(AuthUser authUser) {

    List<Member> members = memberRepository.findByUserId(authUser.getUserId());

    // 각 Member가 참여한 소모임을 모두 조회
    List<GatheringResponseDto> gatheringResponses = new ArrayList<>();

    for (Member member : members) {
      Gathering gathering = findGatheringByMember(member);
      if (gathering != null && (member.getStatus() == MemberStatus.APPROVED)) {
        gatheringResponses.add(GatheringResponseDto.of(gathering));
      }
    }

    // DTO List 변환
    return gatheringResponses;
  }

  // 소모임 수정 로직
  @Transactional
  public GatheringResponseDto updateGathering(AuthUser authUser, Long gatheringId, GatheringRequestDto gatheringRequestDto) {
    // 소모임 조회
    Gathering gathering = findGatheringById(gatheringId);

    // Host인지 권한 체크
    validateHostPermission(authUser, gathering);
    // MaxCount 체크
    validateMaxCount(gatheringRequestDto, gathering);

    // 소모임 정보 업데이트
    gathering.updateDetails(gatheringRequestDto.getTitle(),
        gatheringRequestDto.getDescription(),
        gatheringRequestDto.getGatheringMaxCount(),
        gatheringRequestDto.getLocation());

    // 소모임 저장
    gatheringRepository.save(gathering);

    // 업데이트된 정보를 DTO로 반환
    return GatheringResponseDto.of(gathering);
  }

  @Transactional
  public void deleteGathering(AuthUser authUser, Long gatheringId) {
    // 소모임 조회
    Gathering gathering = findGatheringById(gatheringId);

    // 사용자 권한 검증 (HOST or ADMIN)
    validateHostAndAdminPermission(authUser, gathering);

    // 모임과 관련된 멤버 삭제
    memberRepository.deleteByGathering(gathering); // 모임에 속한 멤버를 삭제하는 메서드

    // 모임 삭제
    gatheringRepository.delete(gathering);
  }

  ////////////////////// 에러 처리를 위한 메서드 ///////////////////////

  private User findUserById(AuthUser authUser) {
    return userRepository.findById(authUser.getUserId()).orElseThrow(
        () -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
  }

  private List<Member> findMembersByUserId(AuthUser authUser) {
    List<Member> members = memberRepository.findByUserId(authUser.getUserId());

    if (members.isEmpty()) {
      throw new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER);
    }
    return members;
  }

  private Gathering findGatheringByMember(Member member) {
    return gatheringRepository.findByMembers(member).orElseThrow(
        () -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING)
    );
  }

  private Gathering findGatheringById(Long gatheringId) {
    return gatheringRepository.findById(gatheringId).orElseThrow(
        () -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING)
    );
  }

  private void validateHostAndAdminPermission(AuthUser authUser, Gathering gathering) {
    // 유저 조회
    User user = findUserById(authUser);
    Member member = null;
    // 유저가 어드민이라면 통과
    if (!(user.getUserRole() == UserRole.ROLE_ADMIN)) {
      // 유저와 소모임을 기반으로 멤버 조회
      member = memberRepository.findByUserAndGathering(user, gathering).orElseThrow(
          () -> new ResponseCodeException(ResponseCode.FORBIDDEN, "권한이 없습니다.")
      );
    }
    // 호스트가 아니거나, 어드민이 아니면 권한 X
    if (!((member == null || (member.getRole() == MemberRole.HOST)))) {
      throw new ResponseCodeException(ResponseCode.FORBIDDEN);
    }
  }

  private void validateHostPermission(AuthUser authUser, Gathering gathering) {
    // 유저 조회
    User user = findUserById(authUser);
    // 유저와 소모임을 기반으로 멤버 조회
    Member member = memberRepository.findByUserAndGathering(user, gathering).orElseThrow(
        () -> new ResponseCodeException(ResponseCode.FORBIDDEN, "권한이 없습니다.")
    );
    // 호스트가 아니면 권한 X
    if ((member.getRole() != MemberRole.HOST)) {
      throw new ResponseCodeException(ResponseCode.FORBIDDEN);
    }
  }

  private static void validateMaxCount(GatheringRequestDto gatheringRequestDto, Gathering gathering) {
    // 변경하려는 최대 인원이 소모임의 현재 인원보다 적은 경우
    if (gathering.getGatheringCount() > gatheringRequestDto.getGatheringMaxCount()) {
      throw new ResponseCodeException(ResponseCode.INVALID_MAX_COUNT);
    }
  }

}
