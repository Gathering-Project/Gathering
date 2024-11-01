package nbc_final.gathering.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String date;
    private String location;
    private Integer maxParticipants;

    private Integer currentParticipants = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    // 정적 팩토리 메서드
    public static Event of(String title, String description, String date, String location, Integer maxParticipants, Gathering gathering, User user) {
        Event event = new Event();
        event.title = title;
        event.description = description;
        event.date = date;
        event.location = location;
        event.maxParticipants = maxParticipants;
        event.gathering = gathering;
        event.user = user;
        return event;
    }

    public void updateEvent(String title, String description, String date, String location, Integer maxParticipants) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.maxParticipants = maxParticipants;
    }

    public void addParticipant(Participant participant) {
        this.participants.add(participant);
        incrementParticipantCount();
    }

    public void removeParticipant(Participant participant) {
        this.participants.remove(participant);
        decrementParticipantCount();
    }

    public void resetCurrentParticipants() {
        this.currentParticipants = 0;
    }

    public void incrementParticipantCount() {
        if (this.currentParticipants < this.maxParticipants) {
            this.currentParticipants++;
        }
    }

    public void decrementParticipantCount() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    public int getCurrentParticipants() {
        return currentParticipants;
    }
}
