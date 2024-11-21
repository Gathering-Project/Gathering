package nbc_final.gathering.domain.gathering.repository;

import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, QuerydslPredicateExecutor<Gathering>, GatheringRepositoryCustom {
    Optional<Gathering> findByMembers(Member member);

    @Query("SELECT g FROM Gathering g WHERE g.userId = :userId")
        // JPQL 쿼리 설정
    List<Gathering> findByUserId(@Param("userId") Long userId);
}
