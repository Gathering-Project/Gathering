package nbc_final.gathering.domain.poll.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.gathering.entity.Gathering;

import java.util.ArrayList;
import java.util.List;

/**
 * Poll : 각 투표
 */
@Entity
@Table(name = "polls")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
public class Poll extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String agenda; // 투표 안건

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private boolean isActive = true; // 투표 활성화 여부

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> options = new ArrayList<>(); // 투표 선택지들

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes = new ArrayList<>(); // 투표에 반영된 각 표들

    // 선택지 추가
    public void addOption(Option option) {
        options.add(option);
        option.setPoll(this);
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    // 투표 마감
    public void finishPoll() {
        this.isActive = false;
    }

    // 투표 생성 정적 팩토리 메서드
    public static Poll createPoll(String agenda, Gathering gathering, Event event, List<String> optionNames) {
        Poll poll = new Poll();
        poll.agenda = agenda;
        poll.gathering = gathering;
        poll.event = event;
        for (String optionName : optionNames) {
            Option option = Option.createOption(poll, optionNames.indexOf(optionName), optionName);
            poll.addOption(option);
        }
        return poll;
    }

}
