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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
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
    void requestAd_success() {
        // Given
        Long userId = 1L;
        Long gatheringId = 1L;
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = startDate.plusDays(2);

        Gathering gathering = mock(Gathering.class);
        when(gatheringRepository.findById(gatheringId)).thenReturn(Optional.of(gathering));
        when(gathering.getUserId()).thenReturn(userId);

        AdCreateRequestDto requestDto = new AdCreateRequestDto(startDate, endDate);

        // When
        AdCreateResponseDto responseDto = adService.requestAd(userId, gatheringId, requestDto);

        // Then
        assertNotNull(responseDto);
        verify(adRepository, times(1)).save(any(Ad.class));
    }

    @Test
    void requestAd_invalidUser_throwsException() {
        // Given
        Long userId = 1L;
        Long gatheringId = 1L;

        Gathering gathering = mock(Gathering.class);
        when(gatheringRepository.findById(gatheringId)).thenReturn(Optional.of(gathering));
        when(gathering.getUserId()).thenReturn(2L); // 다른 유저 ID

        AdCreateRequestDto requestDto = new AdCreateRequestDto(
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(5)
        );

        // When & Then
        ResponseCodeException exception = assertThrows(
                ResponseCodeException.class,
                () -> adService.requestAd(userId, gatheringId, requestDto)
        );

        assertEquals("소모임 소유자만 광고를 생성할 수 있습니다.", exception.getMessage());
    }
}
