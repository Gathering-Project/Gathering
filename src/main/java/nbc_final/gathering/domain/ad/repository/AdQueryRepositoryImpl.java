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

    // 단일 날짜에 대해 겹치는 광고 조회
    @Override
    public List<Ad> findOverlappingAdsByDate(LocalDate checkDate) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.startDate.loe(checkDate)
                        .and(ad.endDate.goe(checkDate))
                        .and(ad.status.notIn(AdStatus.CANCELED, AdStatus.FAILED)))
                .fetch();
    }

    // 소모임 ID와 특정 날짜의 광고 개수 조회
    @Override
    public long countByGatheringIdAndDate(Long gatheringId, LocalDate checkDate) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.gathering.id.eq(gatheringId)
                        .and(ad.startDate.loe(checkDate))
                        .and(ad.endDate.goe(checkDate))
                        .and(ad.status.notIn(AdStatus.CANCELED, AdStatus.FAILED)))
                .fetchCount();
    }

    // CANCELED, FAILED 제외한 전체 광고 수 카운트
    @Override
    public long countAds() {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.status.notIn(AdStatus.CANCELED, AdStatus.FAILED))
                .fetchCount();
    }

    // 날짜 범위 내 겹치는 광고 조회
    @Override
    public List<Ad> findOverlappingAds(LocalDate startDate, LocalDate endDate) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.startDate.loe(endDate)
                        .and(ad.endDate.goe(startDate))
                        .and(ad.status.notIn(AdStatus.CANCELED, AdStatus.FAILED)))
                .fetch();
    }

    // 광고 상태와 날짜 범위에 맞는 광고 조회
    @Override
    public List<Ad> findAdsByStatusAndDateRange(AdStatus status, LocalDate startDate, LocalDate endDate) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.status.eq(status)
                        .and(ad.startDate.loe(endDate))
                        .and(ad.endDate.goe(startDate)))
                .fetch();
    }

    @Override
    public long countOverlappingAds(Long gatheringId, LocalDate startDate, LocalDate endDate) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.gathering.id.eq(gatheringId)
                        .and(ad.startDate.loe(endDate))
                        .and(ad.endDate.goe(startDate))
                        .and(ad.status.notIn(AdStatus.CANCELED, AdStatus.FAILED))) // 제외할 상태
                .fetchCount();
    }

    @Override
    public long countAdsByGatheringAndDate(Long gatheringId, LocalDate date) {
        QAd ad = QAd.ad;

        return queryFactory.selectFrom(ad)
                .where(ad.gathering.id.eq(gatheringId)
                        .and(ad.startDate.loe(date))
                        .and(ad.endDate.goe(date))
                        .and(ad.status.notIn(AdStatus.CANCELED, AdStatus.FAILED)))
                .fetchCount();
    }

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


}
