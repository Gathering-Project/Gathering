package nbc_final.gathering.domain.ad.service;

import nbc_final.gathering.domain.ad.AdExpiryScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AdExpirySchedulerTest {

    @Mock
    private AdService adService;

    @InjectMocks
    private AdExpiryScheduler adExpiryScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체 초기화
    }

    @Test
    void updateAdStatuses_shouldActivateAndExpireAds() {
        // 스케줄러가 호출되었을 때 AdService의 메서드 호출을 검증
        adExpiryScheduler.updateAdStatuses();

        // READY 상태 광고를 ACTIVE로 전환했는지 검증
        verify(adService, times(1)).activatePendingAds();

        // ACTIVE 상태 광고를 EXPIRED로 전환했는지 검증
        verify(adService, times(1)).expireActiveAds();
    }
}
