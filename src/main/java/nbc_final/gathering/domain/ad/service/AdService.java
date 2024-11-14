package nbc_final.gathering.domain.ad.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.ad.dto.AdDetailsDto;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;
import nbc_final.gathering.domain.ad.repository.AdRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.payment.entity.Payment;
import nbc_final.gathering.domain.payment.repository.PaymentRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {

    private static final int MAX_DAILY_ADS = 5;
    private final AdRepository adRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public Ad createAd(Long gatheringId, int durationDays, Long userId, LocalDate startDate) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new RuntimeException("GATHERING_NOT_FOUND"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 광고 신청 가능 날짜 범위 검증
        validateAdDateRange(gatheringId, startDate, startDate.plusDays(durationDays - 1));

        // 광고 생성
        Ad ad = Ad.create(null, gathering, startDate, startDate.plusDays(durationDays - 1));
        adRepository.save(ad);

        // 결제 금액 계산
        Long amount = calculateAdAmount(durationDays);
        Payment payment = Payment.create(user, amount, "광고 결제", gathering);
        payment.setAd(ad);  // 결제와 광고 ID 연결
        paymentRepository.save(payment);

        // 광고 결제 연동
        ad.setPayment(payment);  // 결제와 광고 연결
        adRepository.save(ad);

        return ad;
    }

    private Long calculateAdAmount(int durationDays) {
        return 10000L * durationDays;
    }

    public void validateAdDateRange(Long gatheringId, LocalDate startDate, LocalDate endDate) {
        // LocalDate를 LocalDateTime으로 변환
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            throw new ResponseCodeException(ResponseCode.INVALID_REQUEST, "오늘 이전 날짜는 선택할 수 없습니다.");
        }

        // 변환된 LocalDateTime을 couAdRepositoryntAdsInDateRange에 전달
        long existingAdsCount = adRepository.countAdsInDateRange(gatheringId, startDate, endDate);
        if (existingAdsCount >= MAX_DAILY_ADS) {
            throw new ResponseCodeException(ResponseCode.INVALID_REQUEST, "해당 날짜에는 이미 최대 광고가 예약되었습니다.");
        }
    }


    @Transactional
    public void checkAdExpiry() {
        LocalDate today = LocalDate.now();
        List<Ad> activeAds = adRepository.findByStatusAndEndDateBefore(AdStatus.ACTIVE, today);

        activeAds.forEach(Ad::expireAd);
        adRepository.saveAll(activeAds);  // 전체 저장으로 최적화
    }

    // 광고 상세 정보를 조회하는 메서드
    public AdDetailsDto getAdDetailsById(Long gatheringId, Long adId, Long userId) {
        // 광고를 gatheringId와 adId로 찾음
        Ad ad = adRepository.findByIdAndGatheringId(adId, gatheringId)
                .orElseThrow(() -> new RuntimeException("AD_NOT_FOUND")); // 광고가 없으면 예외 처리

        // Ad를 AdDetailsDto로 변환하여 반환
        return AdDetailsDto.from(ad);
    }

}
