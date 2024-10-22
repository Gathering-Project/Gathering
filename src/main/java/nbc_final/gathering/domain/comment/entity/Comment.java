package nbc_final.gathering.domain.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.user.entity.User;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "comments")
public class Comment extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "comment_content", nullable = false, length = 200)
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //댓글 생성자
    public Comment(String content, Event event, User user) {
        this.content = content;
        this.event = event;
        this.user = user;
    }

    //댓글 수정 메서드
    public void updateContent(String newContent){
        this.content=newContent;
    }

}

