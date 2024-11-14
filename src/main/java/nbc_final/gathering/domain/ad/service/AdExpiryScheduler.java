package nbc_final.gathering.domain.ad.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdExpiryScheduler {

    private final AdService adService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiredAds() {
        adService.checkAdExpiry();  // 매일 자정에 만료 상태 확인 및 업데이트
    }
}
