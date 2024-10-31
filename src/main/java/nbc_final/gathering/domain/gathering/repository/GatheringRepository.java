package nbc_final.gathering.domain.gathering.repository;

import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, QuerydslPredicateExecutor<Gathering>, GatheringRepositoryCustom {
  Optional<Gathering> findByMembers(Member member);

  Optional<Gathering> findByTitle(String title);

  List<Gathering> findAllByTitleContaining(String title);
}
