package nbc_final.gathering.domain.event.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import nbc_final.gathering.domain.gathering.entity.QGathering;
import nbc_final.gathering.domain.member.entity.QMember;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class EventRepositoryCustomImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public EventRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public boolean isUserInGathering(Long gatheringId, Long userId) {
        QGathering gathering = QGathering.gathering;
        QMember member = QMember.member;

        return queryFactory.selectOne()
                .from(gathering)
                .join(gathering.members, member)
                .where(gathering.id.eq(gatheringId)
                        .and(member.user.id.eq(userId))
                        .and(member.status.eq(MemberStatus.APPROVED)))
                .fetchFirst() != null;
    }

    @Override
    public boolean isGatheringCreator(Long userId, Long gatheringId) {
        QGathering gathering = QGathering.gathering;

        return queryFactory.selectOne()
                .from(gathering)
                .where(gathering.id.eq(gatheringId)
                        .and(gathering.userId.eq(userId)))
                .fetchFirst() != null;
    }
}

