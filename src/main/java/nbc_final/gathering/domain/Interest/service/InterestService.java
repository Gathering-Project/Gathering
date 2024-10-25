package nbc_final.gathering.domain.Interest.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.Interest.dto.response.InterestResponseDto;
import nbc_final.gathering.domain.Interest.entity.Interest;
import nbc_final.gathering.domain.Interest.repository.InterestRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

  private final InterestRepository interestRepository;
  private final UserRepository userRepository;

  // 모든 관심사 조회 로직
  public List<InterestResponseDto> getAllInterests(AuthUser authUser) {
    User user = findUserById(authUser);

    // ADMIN인 경우, 현재 등록된 모든 관심사 조회
    if (!(user.getUserRole() == UserRole.ROLE_ADMIN)) {
      throw new ResponseCodeException(ResponseCode.FORBIDDEN);
    }

    return interestRepository.findAll().stream()
        .map(interest -> toResponseDto(interest))
        .collect(Collectors.toList());
  }

  // 관심사 추가 로직
  @Transactional
  public InterestResponseDto addInterest(InterestType interestType) {
    // 중복 체크
    if (interestRepository.existsByInterestType(interestType)) {
      throw new ResponseCodeException(ResponseCode.ALREADY_EXIST_INTEREST);
    }

    Interest interest = new Interest();
    interest.addInterest(interestType);

    Interest savedInterest = interestRepository.save(interest);
    return toResponseDto(savedInterest);
  }

  //////////////////////////////////// 예외 처리를 위한 메서드 //////////////////////////////////////

  // DTO 변환 메서드
  private InterestResponseDto toResponseDto(Interest interest) {
    return new InterestResponseDto(interest.getId(), interest.getInterestType());
  }

  private User findUserById(AuthUser authUser) {
    return userRepository.findById(authUser.getUserId()).orElseThrow(
        () -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
  }


}
