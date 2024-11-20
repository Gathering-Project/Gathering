package nbc_final.gathering.domain.ad.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;
import nbc_final.gathering.domain.ad.entity.QAd;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class AdQueryRepositoryImpl implements AdQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsByGatheringIdAndDate(Long gatheringId, LocalDate date) {
        QAd ad = QAd.ad;

        return queryFactory.selectOne()
                .from(ad)
                .where(ad.gathering.id.eq(gatheringId)
                        .and(ad.startDate.loe(date))
                        .and(ad.endDate.goe(date))
                        .and(ad.status.notIn(AdStatus.CANCELED, AdStatus.FAILED)))
                .fetchFirst() != null;
    }

    @Override
    public long countByDate(LocalDate date) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.startDate.loe(date)
                        .and(ad.endDate.goe(date))
                        .and(ad.status.notIn(AdStatus.CANCELED, AdStatus.FAILED)))
                .fetchCount();
    }

    @Override
    public List<Ad> findAdsByStatusAndEndDate(AdStatus status, LocalDate endDate) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.status.eq(status)
                        .and(ad.endDate.eq(endDate)))
                .fetch();
    }

    @Override
    public List<Ad> findAdsByStatusAndStartDate(AdStatus status, LocalDate startDate) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.status.eq(status)
                        .and(ad.startDate.eq(startDate)))
                .fetch();
    }

    @Override
    public List<Ad> findAdsByStatusesAndDateRange(List<AdStatus> statuses, LocalDate startDate, LocalDate endDate) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.status.in(statuses)
                        .and(ad.startDate.loe(endDate))
                        .and(ad.endDate.goe(startDate)))
                .fetch();
    }


}
