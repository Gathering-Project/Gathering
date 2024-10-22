package nbc_final.gathering.domain.member.repository;

import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUserAndGathering(User user, Gathering gathering);

    Optional<Member> findByUserAndGathering(User user, Gathering gathering);

    boolean existsByUserIdAndGatheringId(Long userId, Long gatheringId);

    List<Member> findAllByGatheringId(Long gatheringId);

    Optional<Member> findByIdAndGatheringId(Long memberId, Long gatheringId);
}
