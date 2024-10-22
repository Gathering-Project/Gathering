package nbc_final.gathering.domain.gathering.repository;

import nbc_final.gathering.domain.gathering.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering, Long> {
}
