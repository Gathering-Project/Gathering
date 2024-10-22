package nbc_final.gathering.domain.gathering.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.gathering.dto.request.GatheringRequestDto;
import nbc_final.gathering.domain.gathering.dto.response.GatheringResponseDto;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.enums.Role;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
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
    Gathering savedGathering = new Gathering(gatheringRequestDto.getTitle(),
        gatheringRequestDto.getDescription(),
        gatheringRequestDto.getGatheringImage(),
        1, gatheringRequestDto.getGatheringMaxCount(),
        BigDecimal.valueOf(50), gatheringRequestDto.getLocation()
    );

    // 주최자 추가
    Member member = new Member(user, savedGathering, Role.HOST);
    savedGathering.getMembers().add(member);

    // 그룹 저장
    gatheringRepository.save(savedGathering);
    memberRepository.save(member);

    return new GatheringResponseDto(savedGathering);
  }

  /*
    {
      title: "소모임 제목",
      description: "소모임 설명",
      groupMaxCount: 10
    }
  */
  // 소모임 단건 조회 로직
  public GatheringResponseDto getGathering(AuthUser authUser, Long gatheringId) {

    List<Member> members = findMembersByUserId(authUser);

    // 유저가 조회하고자 하는 ID 소모임 조회
    for (Member member : members) {
      Gathering gathering = member.getGathering();
      if (gathering != null && gathering.getId().equals(gatheringId)) {
        return new GatheringResponseDto(gathering);
      }
    }
    throw new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING);
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



}
