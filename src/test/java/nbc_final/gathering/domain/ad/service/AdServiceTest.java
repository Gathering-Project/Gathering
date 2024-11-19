package nbc_final.gathering.domain.ad.service;

import nbc_final.gathering.domain.ad.dto.request.AdCreateRequestDto;
import nbc_final.gathering.domain.ad.dto.response.AdCreateResponseDto;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;
import nbc_final.gathering.domain.ad.repository.AdRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.common.exception.ResponseCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdServiceTest {

    @InjectMocks
    private AdService adService;

    @Mock
    private AdRepository adRepository;

    @Mock
    private GatheringRepository gatheringRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("광고 요청 성공")
    void adRequestSuccess() {
        // given
        Long userId = 1L;
        Long gatheringId = 1L;
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = startDate.plusDays(2);

        Gathering gathering = createGathering();
        when(gatheringRepository.findById(gatheringId)).thenReturn(Optional.of(gathering));

        AdCreateRequestDto requestDto = AdCreateRequestDto.of(startDate, endDate);

        // when
        AdCreateResponseDto responseDto = adService.requestAd(userId, gatheringId, requestDto);

        // then
        assertNotNull(responseDto);
        verify(adRepository, times(1)).save(any(Ad.class));
    }

    @Test
    @DisplayName("유효하지 않은 사용자 예외")
    void invalidUserException() {
        // given
        Long userId = 1L;
        Long gatheringId = 1L;

        Gathering gathering = createGathering();
        when(gatheringRepository.findById(gatheringId)).thenReturn(Optional.of(gathering));
        when(gathering.getUserId()).thenReturn(2L); // 다른 유저 ID

        AdCreateRequestDto requestDto = AdCreateRequestDto.of(
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(5)
        );

        // when & then
        ResponseCodeException exception = assertThrows(
                ResponseCodeException.class,
                () -> adService.requestAd(userId, gatheringId, requestDto)
        );

        assertEquals("소모임 소유자만 광고를 생성할 수 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("광고 상태 및 기간 조회")
    void findAdsByStatusAndDate() {
        // given
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 10);

        Ad activeAd = createAd(AdStatus.ACTIVE, startDate, endDate);
        Ad pendingAd = createAd(AdStatus.PENDING, startDate, endDate);
        when(adRepository.findAdsByStatusesAndDateRange(
                List.of(AdStatus.ACTIVE, AdStatus.PENDING), startDate, endDate)
        ).thenReturn(List.of(activeAd, pendingAd));

        // when
        List<AdStatus> statuses = List.of(AdStatus.ACTIVE, AdStatus.PENDING);
        List<Ad> result = adRepository.findAdsByStatusesAndDateRange(statuses, startDate, endDate);

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(ad -> ad.getStatus() == AdStatus.ACTIVE));
        assertTrue(result.stream().anyMatch(ad -> ad.getStatus() == AdStatus.PENDING));
    }

    private Ad createAd(AdStatus status, LocalDate startDate, LocalDate endDate) {
        Gathering gathering = createGathering();
        Ad ad = Ad.of(gathering, startDate, endDate, 10000L);
        ad.updateStatus(status);
        return ad;
    }

    private Gathering createGathering() {
        Gathering gathering = mock(Gathering.class);
        when(gathering.getId()).thenReturn(1L);
        when(gathering.getUserId()).thenReturn(1L);
        return gathering;
    }
}
