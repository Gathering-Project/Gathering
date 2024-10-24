package nbc_final.gathering.domain.attachment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.user.entity.User;

@Entity
@Getter
@Setter
@Table(name = "attachments")
@NoArgsConstructor
public class Attachment extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long id;

    @Column(nullable = false)
    private String profileImagePath; // S3 저장된 이미지의 URL을 저장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    // 유저와 이미지 URL을 받는 생성자
    public Attachment(User user, Gathering gathering, String fileUrl) {
        this.user = user;
        this.gathering = gathering;
        this.profileImagePath = fileUrl;
    }
}