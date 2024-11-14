package nbc_final.gathering.domain.ad.repository;

import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdRepository extends JpaRepository<Ad, Long> {

    @Query("SELECT COUNT(a) FROM Ad a WHERE a.gathering.id = :gatheringId AND FUNCTION('DATE', a.startDate) = :startDate")
    long countByGatheringAndDate(@Param("gatheringId") Long gatheringId, @Param("startDate") LocalDate startDate);

    @Query("SELECT COUNT(a) FROM Ad a WHERE a.gathering.id = :gatheringId AND a.startDate >= :startDate AND a.endDate <= :endDate")
    long countAdsInDateRange(@Param("gatheringId") Long gatheringId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    List<Ad> findByStatusAndEndDateBefore(AdStatus status, LocalDate endDate);

    @Query("SELECT a FROM Ad a WHERE a.gathering.id = :gatheringId AND :date BETWEEN a.startDate AND a.endDate")
    List<Ad> findAdsByGatheringIdAndDate(@Param("gatheringId") Long gatheringId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Ad a WHERE a.adId = :adId AND a.gathering.id = :gatheringId")
    Optional<Ad> findByIdAndGatheringId(@Param("adId") Long adId, @Param("gatheringId") Long gatheringId);

}
