package nbc_final.gathering.domain.example.attachment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.entity.TimeStamped;
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
    @JoinColumn(name = "user_id")
    private User user;

    public Attachment(AuthUser user, String fileUrl) {
        super();
    }

    public void setUser(Long userId) {
    }

    //    @ManyToOne
//    @JoinColumn(name = "group_id")
//    private Group group


}