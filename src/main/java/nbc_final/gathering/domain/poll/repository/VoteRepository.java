package nbc_final.gathering.domain.poll.repository;

import nbc_final.gathering.domain.poll.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

}
