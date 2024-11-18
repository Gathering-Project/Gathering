package nbc_final.gathering.domain.ad;

import nbc_final.gathering.domain.ad.service.AdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

class AdExpirySchedulerTest {

    @Mock
    private AdService adService;

    @Mock
    private Logger logger;

    @InjectMocks
    private AdExpiryScheduler adExpiryScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateAdStatuses_withLogging() {
        // given
        doNothing().when(adService).activatePendingAds();
        doNothing().when(adService).expireActiveAds();

        // when
        adExpiryScheduler.updateAdStatuses();

        // then
        verify(logger, times(1)).info("스케줄러 실행: 광고 상태 업데이트 시작");
        verify(adService, times(1)).activatePendingAds();
        verify(logger, times(1)).info("READY 상태 광고 -> ACTIVE로 업데이트 완료");
        verify(adService, times(1)).expireActiveAds();
        verify(logger, times(1)).info("ACTIVE 상태 광고 -> EXPIRED로 업데이트 완료");
        verify(logger, times(1)).info("스케줄러 실행 완료: 광고 상태 업데이트 종료");
    }
}
