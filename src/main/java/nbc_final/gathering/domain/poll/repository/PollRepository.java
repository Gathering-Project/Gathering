package nbc_final.gathering.domain.poll.repository;

import nbc_final.gathering.domain.poll.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Long> {

}
