package nbc_final.gathering.domain.ad.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.ad.dto.AdDetailsDto;
import nbc_final.gathering.domain.ad.dto.request.AdCreateRequestDto;
import nbc_final.gathering.domain.ad.dto.response.AdCreateResponseDto;
import nbc_final.gathering.domain.ad.dto.response.AdListResponseDto;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;
import nbc_final.gathering.domain.ad.entity.QAd;
import nbc_final.gathering.domain.ad.repository.AdRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.payment.entity.Payment;
import nbc_final.gathering.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final GatheringRepository gatheringRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public AdCreateResponseDto requestAd(Long userId, Long gatheringId, AdCreateRequestDto requestDto) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING, "소모임을 찾을 수 없습니다."));

        if (!gathering.getUserId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN, "소모임 소유자만 광고를 생성할 수 있습니다.");
        }

        validateStartDate(requestDto.getStartDate());
        validateAdDuration(requestDto.getStartDate(), requestDto.getEndDate());
        validateAdDateRange(gatheringId, requestDto.getStartDate(), requestDto.getEndDate());

        // 광고 금액 계산
        long adDuration = ChronoUnit.DAYS.between(requestDto.getStartDate(), requestDto.getEndDate()) + 1;
        long totalAmount = adDuration * 10000L;

        // 광고 생성
        Ad ad = Ad.create(gathering, requestDto.getStartDate(), requestDto.getEndDate(), totalAmount);

        // 결제 주문서 생성
        Payment payment = Payment.create(totalAmount, "광고 결제", gathering, requestDto.getStartDate(), requestDto.getEndDate());
        payment.setAd(ad); // 광고와 결제 연결
        ad.setPayment(payment);

        // 저장
        adRepository.save(ad);

        log.info("광고 생성 완료: Ad ID = {}, Payment ID = {}", ad.getAdId(), payment.getPaymentId());

        return AdCreateResponseDto.of(
                ad.getAdId(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                totalAmount,
                ad.getOrderName(),
                adDuration
        );
    }



    private void validateStartDate(LocalDate startDate) {
        LocalDate earliestAllowedDate = LocalDate.now().plusDays(2); // 현재 날짜로부터 2일 뒤
        if (startDate.isBefore(earliestAllowedDate)) {
            throw new ResponseCodeException(ResponseCode.INVALID_START_DATE,
                    String.format("광고 시작 날짜는 최소 %s 이후여야 합니다.", earliestAllowedDate));
        }
    }

    private void validateAdDuration(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseCodeException(ResponseCode.INVALID_REQUEST, "광고 종료 날짜는 시작 날짜 이후여야 합니다.");
        }

        long duration = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (duration < 1) {
            throw new ResponseCodeException(ResponseCode.INVALID_REQUEST, "광고는 최소 하루 이상이어야 합니다.");
        }
    }


    @Transactional
    public void validateAdDateRange(Long gatheringId, LocalDate startDate, LocalDate endDate) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        for (int i = 0; i < daysBetween; i++) {
            LocalDate currentDate = startDate.plusDays(i);

            // 소모임별 같은 날짜 광고 존재 여부 확인
            boolean adExistsForGathering = adRepository.existsByGatheringIdAndDate(gatheringId, currentDate);
            if (adExistsForGathering) {
                throw new ResponseCodeException(ResponseCode.INVALID_REQUEST,
                        String.format("소모임 '%s'의 날짜 %s에 이미 광고가 등록되어 추가할 수 없습니다.", gatheringId, currentDate));
            }

            // 날짜별 광고 개수 확인 (최대 5개 소모임)
            long adsCountForDate = adRepository.countByDate(currentDate);
            if (adsCountForDate >= 5) {
                throw new ResponseCodeException(ResponseCode.INVALID_REQUEST,
                        String.format("날짜 '%s'에는 이미 최대 5개의 소모임이 광고를 등록하여 추가할 수 없습니다.", currentDate));
            }
        }

        log.info("광고 날짜 검증 완료: Gathering ID = {}, StartDate = {}, EndDate = {}", gatheringId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public AdListResponseDto getAdsWithinPeriod(LocalDate startDate, LocalDate endDate) {
        // 주어진 날짜 범위 내에서 ACTIVE 상태의 광고 조회
        List<Ad> ads = adRepository.findAdsByStatusAndDateRange(AdStatus.ACTIVE, startDate, endDate);

        // 광고 세부 정보 변환
        List<AdDetailsDto> adDetailsList = ads.stream()
                .map(AdDetailsDto::from)
                .toList();

        // 응답 반환
        return new AdListResponseDto(adDetailsList, adDetailsList.size());
    }

    @Transactional
    public void activatePendingAds() {
        LocalDate today = LocalDate.now();
        List<Ad> readyAds = adRepository.findAdsByStatusAndDateRange(AdStatus.READY, today, today);

        readyAds.forEach(ad -> {
            ad.updateStatus(AdStatus.ACTIVE);
            adRepository.save(ad);
        });

        log.info("READY 상태 광고가 ACTIVE로 전환되었습니다. 총 {}개 광고", readyAds.size());
    }

    @Transactional
    public void expireActiveAds() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Ad> activeAds = adRepository.findAdsByStatusAndDateRange(AdStatus.ACTIVE, yesterday, yesterday);

        activeAds.forEach(ad -> {
            ad.updateStatus(AdStatus.EXPIRED);
            adRepository.save(ad);
        });

        log.info("ACTIVE 상태 광고가 EXPIRED로 전환되었습니다. 총 {}개 광고", activeAds.size());
    }

    private int calculateAdAmount(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // 시작일 포함
        return (int) days * 10000; // 하루당 10000원
    }


    @Transactional
    public void updateAdStatusToReady(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_AD, "결제와 연결된 광고를 찾을 수 없습니다."));

        ad.updateStatus(AdStatus.READY);
        adRepository.save(ad);

        log.info("광고 상태 업데이트 완료: Ad ID = {}", ad.getAdId());
    }

    @Transactional(readOnly = true)
    public AdDetailsDto getAdDetails(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_AD, "광고를 찾을 수 없습니다."));

        return AdDetailsDto.from(ad); // Ad -> AdDetailsDto로 변환
    }
}
