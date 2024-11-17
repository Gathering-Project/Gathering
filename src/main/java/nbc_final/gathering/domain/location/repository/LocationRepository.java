package nbc_final.gathering.domain.location.repository;

import nbc_final.gathering.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
