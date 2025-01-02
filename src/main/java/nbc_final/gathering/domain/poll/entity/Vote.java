package nbc_final.gathering.domain.poll.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.user.entity.User;
import org.springframework.data.jpa.repository.Lock;

/**
 * Vote : 각 개인의 표
 */
@Entity
@Table(name = "votes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote extends TimeStamped {

    @EmbeddedId
    private VoteId id; // 복합 PK: poll(투표) ID + user ID


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    // @MapsId :  FK를 PK로 지정할 때 사용하는 어노테이션
    @MapsId("pollId") // 복합 키의 pollId와 연결
    private Poll poll;

    @ManyToOne
    @MapsId("userId") // 복합 키의 userId와 연결
    @JoinColumn(name = "user_id")
    private User user;

    private int selectedOption; // 선택한 옵션의 인덱스 (0, 1, 2...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private boolean isDone = true; // 투표 완료 여부

    @Version
    private Long version;

    // 투표 안한 상태로 초기화
    public void resetStatus() {
        this.isDone = false;
    }

    // 투표한 상태로 업데이트
    public void updateStatus() {
        this.isDone = true;
    }

    // 이미 투표한 상태에서 선택지 변경
    public void updateSelectedOption(int newOptionNum) {
        this.selectedOption = newOptionNum;
    }

    // Vote 복합 PK인 ID 지정(ID : poll_id  + user_id)
    public void setId(VoteId voteId) {
        this.id = voteId;
    }

    // 투표 참여
    public static Vote castVote(Poll poll, User user, Gathering gathering, Event event, int selectedOption) {
        Vote vote = new Vote();
        vote.poll = poll;
        vote.user = user;
        vote.gathering = gathering;
        vote.event = event;
        vote.selectedOption = selectedOption;
        VoteId voteId = new VoteId(poll.getId(), user.getId());
        vote.setId(voteId);
        return vote;
    }
}
