package nbc_final.gathering.domain.poll.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "poll_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Option{

    @EmbeddedId
    private OptionId id;

    @Column(nullable = false, length = 255)
    private String name; // 선택지 이름

    @Column(nullable = false)
    private int voteCount = 0; // 득표수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    @JsonIgnore
    @MapsId("pollId")
    private Poll poll;

    public static Option createOption(Poll poll, int optionNum, String name) {
        Option option = new Option();
        option.poll = poll;
        option.name = name;
        option.id = new OptionId(poll.getId(), optionNum); // OptionId를 생성하여 id에 할당
        return option;
    }

    // 득표 수 증가
    public void incrementVoteCount() {
        this.voteCount++;
    }

    // 득표 수 감소
    public void decreaseVoteCount() {
        this.voteCount--;
    }

    // 투표에 선택지 추가
    public void setPoll(Poll poll) {
        this.poll = poll;
    }
}
