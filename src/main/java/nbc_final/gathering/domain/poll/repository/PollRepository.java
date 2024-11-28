package nbc_final.gathering.domain.poll.repository;

import nbc_final.gathering.domain.poll.entity.Poll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PollRepository extends JpaRepository<Poll, Long> {

    // ( Option을 조회하기 위해 페이지에 존재하는 Poll 숫자만큼 N + 1 문제 발생)
    Page<Poll> findAllByEventId(Long eventId, Pageable pageable);

    // 특정 이벤트 안의 모든 투표 조회(각 투표의 옵션 포함)
    // Fetch Join으로 N + 1 문제 해결 => ~ToMany 관계에서 Fetch Join과 Paging을 같이 썼기 때문에 OOM 발생 가능성
//    @Query(value = "SELECT DISTINCT p FROM Poll p LEFT JOIN FETCH p.options WHERE p.event.id = :eventId",
//            countQuery = "SELECT COUNT(p) FROM Poll p WHERE p.event.id = :eventId")
//    Page<Poll> findAllWithOptionsByEventId(@Param("eventId") Long eventId, Pageable pageable);
}
