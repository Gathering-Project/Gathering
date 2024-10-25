package nbc_final.gathering.domain.userInterest.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.Interest.entity.Interest;
import nbc_final.gathering.domain.Interest.repository.InterestRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import nbc_final.gathering.domain.userInterest.dto.request.UserInterestRequestDto;
import nbc_final.gathering.domain.userInterest.dto.response.UserInterestResponseDto;
import nbc_final.gathering.domain.userInterest.repository.UserInterestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserInterestService {

  private final UserRepository userRepository;
  private final InterestRepository interestRepository;

  // 관심사 추가 로직
  @Transactional
  public UserInterestResponseDto addUserInterest(AuthUser authUser, UserInterestRequestDto requestDto) {

    // 유저 조회
    User user = findUserById(authUser);

    // 관심사 조회
    Interest interest = findInterestByInterestType(requestDto);

    // 사용자와 관심사 관계 추가
    user.addUserInterest(interest);
    interest.addUserInterest(user);

    // 관심사 추가 후 응답 DTO 생성
    return UserInterestResponseDto.of(interest);
  }

  ////////////////////////////////////// 예외 처리를 위한 메서드 ////////////////////////////////////

  private Interest findInterestByInterestType(UserInterestRequestDto requestDto) {
    return interestRepository.findByInterestType(requestDto.getInterestType())
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_INTEREST));
  }

  private User findUserById(AuthUser authUser) {
    return userRepository.findById(authUser.getUserId())
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
  }


}
