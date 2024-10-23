package nbc_final.gathering.domain.event.repository;

import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.event.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    Optional<Participant> findByEventAndUserId(Event event, Long userId);

    List<Participant> findAllByEvent(Event event);
}
