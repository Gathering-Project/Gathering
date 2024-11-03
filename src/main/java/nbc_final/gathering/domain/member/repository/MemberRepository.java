package nbc_final.gathering.domain.member.repository;

import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUserAndGathering(User user, Gathering gathering);

    Optional<Member> findByUserAndGathering(User user, Gathering gathering);

    boolean existsByUserIdAndGatheringId(Long userId, Long gatheringId);

    List<Member> findAllByGatheringId(Long gatheringId);

    Optional<Member> findByIdAndGatheringId(Long memberId, Long gatheringId);

    Optional<Member> findByUserIdAndGatheringId(Long userId, Long gatheringId);

    List<Member> findByUserId(Long userId);

    Optional<Member> findByUserIdAndGatheringIdAndStatus(Long userId, Long gatheringId, MemberStatus status);


    // 승인된 멤버를 조회하는 메서드 추가
    List<Member> findAllByGatheringAndStatus(Gathering gathering, MemberStatus status);

  @Modifying
  @Query("DELETE FROM Member m WHERE m.gathering = :gathering")
  void deleteByGathering(@Param("gathering") Gathering gathering);
}

