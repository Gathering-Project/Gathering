package nbc_final.gathering.domain.ad.repository;

import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdQueryRepository {

    // 단일 날짜에 대해 겹치는 광고 조회
    List<Ad> findOverlappingAdsByDate(LocalDate checkDate);

    // 소모임 ID와 날짜로 광고 찾기
    long countByGatheringIdAndDate(Long gatheringId, LocalDate checkDate);

    // 전체 광고 카운트 (CANCELED, FAILED 상태 제외)
    long countAds();

    long countAdsByGatheringAndDate(Long gatheringId, LocalDate date);


    // 날짜 범위 내 겹치는 광고 조회
    List<Ad> findOverlappingAds(LocalDate startDate, LocalDate endDate);

    // 광고 상태와 날짜 범위 내 광고 조회
    List<Ad> findAdsByStatusAndDateRange(AdStatus status, LocalDate startDate, LocalDate endDate);

    long countOverlappingAds(Long gatheringId, LocalDate startDate, LocalDate endDate);

    boolean existsByGatheringIdAndDate(Long gatheringId, LocalDate date);
    long countByDate(LocalDate date);


}
