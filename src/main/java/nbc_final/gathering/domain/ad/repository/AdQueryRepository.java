package nbc_final.gathering.domain.ad.repository;

import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;

import java.time.LocalDate;
import java.util.List;

public interface AdQueryRepository {

    boolean existsByGatheringIdAndDate(Long gatheringId, LocalDate date);

    long countByDate(LocalDate date);

    List<Ad> findAdsByStatusAndEndDate(AdStatus status, LocalDate endDate);

    List<Ad> findAdsByStatusAndStartDate(AdStatus status, LocalDate startDate);

    List<Ad> findAdsByStatusesAndDateRange(List<AdStatus> statuses, LocalDate startDate, LocalDate endDate);
}
