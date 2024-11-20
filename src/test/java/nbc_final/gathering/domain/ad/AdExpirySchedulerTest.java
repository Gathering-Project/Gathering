package nbc_final.gathering.domain.ad;

import nbc_final.gathering.domain.ad.service.AdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class AdExpirySchedulerTest {

    @Mock
    private AdService adService;

    @InjectMocks
    private AdExpiryScheduler adExpiryScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("광고 상태 업데이트 스케줄러 테스트")
    void updateAdStatusesScheduler() {
        // given
        doNothing().when(adService).activatePendingAds();
        doNothing().when(adService).expireActiveAds();

        // when
        adExpiryScheduler.updateAdStatuses();

        // then
        verify(adService, times(1)).activatePendingAds();
        verify(adService, times(1)).expireActiveAds();
    }
}
