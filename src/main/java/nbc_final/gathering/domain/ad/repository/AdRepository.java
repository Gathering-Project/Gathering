package nbc_final.gathering.domain.ad.repository;

import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long>, AdQueryRepository {

    // 광고 상태와 날짜 범위에 맞는 광고 조회
    List<Ad> findAdsByStatusAndDateRange(AdStatus status, LocalDate startDate, LocalDate endDate);

    // 소모임 ID와 상태별 날짜 범위 내 광고 개수 카운트
    long countByGatheringIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(
            Long gatheringId, LocalDate endDate, LocalDate startDate, List<AdStatus> statuses);

    @Modifying
    @Query("UPDATE Ad a SET a.status = :status WHERE a.adId = :adId")
    int updateAdStatus(@Param("adId") Long adId, @Param("status") AdStatus status);

}
