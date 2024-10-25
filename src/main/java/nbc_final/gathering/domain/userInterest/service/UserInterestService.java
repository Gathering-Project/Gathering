package nbc_final.gathering.domain.userInterest.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.Interest.entity.Interest;
import nbc_final.gathering.domain.Interest.repository.InterestRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.user.repository.UserRepository;
import nbc_final.gathering.domain.userInterest.dto.request.UserInterestRequestDto;
import nbc_final.gathering.domain.userInterest.dto.response.UserInterestResponseDto;
import nbc_final.gathering.domain.userInterest.entity.UserInterest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInterestService {

  private final UserRepository userRepository;
  private final InterestRepository interestRepository;

  // 관심사 추가 로직
  @Transactional
  public List<UserInterestResponseDto> addUserInterest(AuthUser authUser, UserInterestRequestDto requestDto) {
    // 유저 조회
    User user = findUserById(authUser);

    // List<Interest> 형식으로 변환
    List<Interest> interests = findInterestsByInterestTypes(requestDto);

    // 유저와 관심사 각각에 입력
    for (Interest interest : interests) {
      user.addUserInterest(interest);
      interest.addUserInterest(user);
    }

    List<UserInterestResponseDto> responseDtos = new ArrayList<>();

    return responseDtos;
  }

  ////////////////////////////////////// 예외 처리를 위한 메서드 ////////////////////////////////////

  private List<Interest> findInterestsByInterestTypes(UserInterestRequestDto requestDto) {
    // 요청한 관심사들을 Interest로 변환
    List<String> interests = requestDto.getInterestTypes();

    List<Interest> interestList = new ArrayList<>();

    for (String interestTypeString : interests) {
      // enum값에 있는지 확인
      InterestType interestType = InterestType.valueOf(interestTypeString);
      // DB에 확인 후에 있으면 리스트에 추가
      Interest interest = interestRepository.findByInterestType(interestType)
          .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_INTEREST));
      interestList.add(interest);
    }
    return interestList;
  }

  private User findUserById(AuthUser authUser) {
    return userRepository.findById(authUser.getUserId())
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
  }
}
