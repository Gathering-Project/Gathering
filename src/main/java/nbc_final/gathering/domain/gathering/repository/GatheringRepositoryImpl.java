package nbc_final.gathering.domain.gathering.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import static nbc_final.gathering.domain.gathering.entity.QGathering.gathering;

public class GatheringRepositoryImpl implements GatheringRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    
    public GatheringRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Gathering> searchGatheringsByTitle(String title) {
        return queryFactory.selectFrom(gathering)
                .where(gathering.title.containsIgnoreCase(title))
                .fetch();
    }

    @Override
    public List<Gathering> searchGatheringsByLocation(String location) {
        return queryFactory.selectFrom(gathering)
                .where(gathering.location.eq(location))
                .fetch();
    }

    @Override
    public List<Gathering> searchGatheringsByTitleAndLocation(String title, String location) {
        return queryFactory.selectFrom(gathering)
                .where(
                        gathering.title.containsIgnoreCase(title)
                                .and(gathering.location.eq(location))
                )
                .fetch();
    }

}
