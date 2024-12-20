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
import nbc_final.gathering.domain.ad.repository.AdRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.payment.entity.Payment;
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

    // 광고 생성
    @Transactional
    public AdCreateResponseDto requestAd(Long userId, Long gatheringId, AdCreateRequestDto requestDto) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        if (!gathering.getUserId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        validateStartDate(requestDto.getStartDate());
        validateAdDuration(requestDto.getStartDate(), requestDto.getEndDate());
        validateAdDateRange(gatheringId, requestDto.getStartDate(), requestDto.getEndDate());

        // 광고 금액 계산
        long adDuration = ChronoUnit.DAYS.between(requestDto.getStartDate(), requestDto.getEndDate()) + 1;
        long totalAmount = adDuration * 10000L;

        // 광고 생성
        Ad ad = Ad.of(gathering, requestDto.getStartDate(), requestDto.getEndDate(), totalAmount);


        // 결제 주문서 생성
        Payment payment = Payment.of(totalAmount, "광고 결제", gathering, requestDto.getStartDate(), requestDto.getEndDate());
        payment.setAd(ad); // 광고와 결제 연결
        ad.setPayment(payment);

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

    //광고 날짜 범위 검증
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

            // 날짜별 광고 개수 확인 (최대 5개)
            long adsCountForDate = adRepository.countByDate(currentDate);
            if (adsCountForDate >= 5) {
                throw new ResponseCodeException(ResponseCode.INVALID_REQUEST,
                        String.format("날짜 '%s'에는 이미 최대 5개의 소모임이 광고를 등록하여 추가할 수 없습니다.", currentDate));
            }
        }
    }

    // 특정 날짜 범위 내 광고 조회
    @Transactional(readOnly = true)
    public AdListResponseDto getAdsWithinPeriod(LocalDate startDate, LocalDate endDate) {
        // 조회 상태 목록 설정 (ACTIVE, PENDING)
        List<AdStatus> statuses = List.of(AdStatus.ACTIVE, AdStatus.PENDING, AdStatus.PAID, AdStatus.EXPIRED);

        // QueryDSL 메서드 호출
        List<Ad> ads = adRepository.findAdsByStatusesAndDateRange(statuses, startDate, endDate);
        List<AdDetailsDto> adDetailsList = ads.stream()
                .map(AdDetailsDto::from)
                .toList();

        return AdListResponseDto.of(adDetailsList, adDetailsList.size());
    }

    // READY 상태 광고를 ACTIVE로 전환
    @Transactional
    public void activatePendingAds() {
        LocalDate today = LocalDate.now();

        // startDate가 오늘인 광고를 조회
        List<Ad> readyAds = adRepository.findAdsByStatusAndStartDate(AdStatus.PAID, today);

        // READY 상태를 ACTIVE로 업데이트
        readyAds.forEach(ad -> {
            ad.updateStatus(AdStatus.ACTIVE);
            adRepository.save(ad);
        });

        log.info("startDate가 오늘인 READY 상태 광고가 ACTIVE로 전환되었습니다. 총 {}개 광고", readyAds.size());
    }

    // ACTIVE 상태 광고를 EXPIRED로 전환
    @Transactional
    public void expireActiveAds() {
        LocalDate today = LocalDate.now();

        // 오늘 날짜가 endDate인 광고를 검색하여 상태를 업데이트
        List<Ad> activeAds = adRepository.findAdsByStatusAndEndDate(AdStatus.ACTIVE, today);

        activeAds.forEach(ad -> {
            ad.updateStatus(AdStatus.EXPIRED);
            adRepository.save(ad);
        });

        log.info("endDate가 오늘인 ACTIVE 상태 광고가 EXPIRED로 전환되었습니다. 총 {}개 광고", activeAds.size());
    }

    // 광고 상태를 PAID로 업데이트
    @Transactional
    public void updateAdStatusToReady(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_AD, "결제와 연결된 광고를 찾을 수 없습니다."));

        ad.updateStatus(AdStatus.PAID);
        adRepository.save(ad);

        log.info("광고 상태 업데이트 완료: Ad ID = {}", ad.getAdId());
    }

    // 광고 세부 정보 조회
    @Transactional(readOnly = true)
    public AdDetailsDto getAdDetails(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_AD));
        return AdDetailsDto.from(ad);
    }


// ------- 유틸 메서드 -------

    // 광고 시작 날짜 검증
    private void validateStartDate(LocalDate startDate) {
        LocalDate earliestAllowedDate = LocalDate.now().plusDays(2); // 현재 날짜로부터 2일 뒤
        if (startDate.isBefore(earliestAllowedDate)) {
            throw new ResponseCodeException(ResponseCode.INVALID_START_DATE,
                    String.format("광고 시작 날짜는 최소 %s 이후여야 합니다.", earliestAllowedDate));
        }
    }

    // 광고 기간 검증
    private void validateAdDuration(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseCodeException(ResponseCode.AD_INVALID_DURATION);
        }

        long duration = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (duration < 1) {
            throw new ResponseCodeException(ResponseCode.INVALID_REQUEST);
        }
    }
}
