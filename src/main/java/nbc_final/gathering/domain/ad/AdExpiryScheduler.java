package nbc_final.gathering.domain.ad;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.domain.ad.service.AdService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdExpiryScheduler {

    private final AdService adService;

    /**
     * 매일 자정에 READY 상태 광고를 ACTIVE로 전환 및 ACTIVE 상태 광고를 EXPIRED로 전환
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void updateAdStatuses() {
        adService.activatePendingAds(); // startDate가 오늘인 READY 광고를 ACTIVE로 전환
        adService.expireActiveAds(); // endDate가 오늘인 ACTIVE 광고를 EXPIRED로 전환
    }
}
