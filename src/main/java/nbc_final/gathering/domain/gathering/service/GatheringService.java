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
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
        "임의 이미지 URL",
        1, gatheringRequestDto.getGatheringMaxCount(),
        BigDecimal.valueOf(50), gatheringRequestDto.getLocation()
        );

    // 주최자 추가
    Member member = new Member(user, savedGathering, MemberRole.HOST);
    savedGathering.getMembers().add(member);

    // 그룹 저장
    gatheringRepository.save(savedGathering);
    memberRepository.save(member);

    return new GatheringResponseDto(savedGathering);
  }

  ////////////////////// 에러 처리를 위한 메서드 ///////////////////////

  private User findUserById(AuthUser authUser) {
    return userRepository.findById(authUser.getUserId()).orElseThrow(
        () -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
  }
}
