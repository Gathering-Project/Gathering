package nbc_final.gathering.domain.poll.repository;

import jakarta.persistence.LockModeType;
import nbc_final.gathering.domain.poll.entity.Option;
import nbc_final.gathering.domain.poll.entity.OptionId;
import nbc_final.gathering.domain.poll.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OptionRepository extends JpaRepository<Option, OptionId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Option o WHERE o.poll = :poll AND o.id.optionNum = :optionNum")
    Option findByPollAndOptionNum(Poll poll, int optionNum);

}
