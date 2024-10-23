package nbc_final.gathering.domain.event.repository;

import nbc_final.gathering.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {

    List<Event> findAllByGatheringId(Long gatheringId);

}
