package nbc_final.gathering.domain.Interest.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.domain.Interest.entity.Interest;
import nbc_final.gathering.domain.Interest.repository.InterestRepository;
import nbc_final.gathering.domain.user.enums.InterestType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterestDataInitializer implements CommandLineRunner {

  private final InterestRepository interestRepository;

  // 애플리에키션 시작과 함께 enum에 있는 값들 DB에 저장
  @Override
  public void run(String... args) {
    for (InterestType interestType : InterestType.values()) {
      // DB에 해당 InterestType이 없을 경우에만 추가
      if (!interestRepository.existsByInterestType(interestType)) {
        interestRepository.save(new Interest(interestType));
      }
    }
  }
}
