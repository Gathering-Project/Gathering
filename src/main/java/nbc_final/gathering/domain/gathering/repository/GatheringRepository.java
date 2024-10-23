package nbc_final.gathering.domain.gathering.repository;

import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GatheringRepository extends JpaRepository<Gathering, Long> {
  Optional<Gathering> findByMembers(Member member);
}
