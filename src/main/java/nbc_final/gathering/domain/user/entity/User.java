package nbc_final.gathering.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.example.attachment.entity.Attachment;
import nbc_final.gathering.domain.user.dto.request.UserUpdateRequestDto;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.user.enums.MbtiType;
import nbc_final.gathering.domain.user.enums.UserRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
public class User extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String location;

    private String nickname;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    private InterestType interestType;

    @Enumerated(EnumType.STRING)
    private MbtiType mbtiType;

    private LocalDateTime withdrawalDate; // 탈퇴일

    @Enumerated(EnumType.STRING)
    private UserRole userRole; // 운영자/일반 유저

//    @Lob
//    private byte[] profileImage; //

    // 이미지 파일 경로
    private String profileImagePath;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Member> members = new ArrayList<>();

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Event> events = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Attachment attachment;

    @Builder
    public User(
            Long id,
            String nickname,
            String email,
            String password,
            UserRole userRole
    ) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    // 생성자: userRole을 UserRole로 받음
    public User(String email, String password, UserRole userRole, String nickname) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.nickname = nickname;
    }

    public void updateIsDeleted() {
        this.withdrawalDate = LocalDateTime.now();
        this.isDeleted = true;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateInfo(UserUpdateRequestDto requestDto) {
        this.location = requestDto.getLocation();
        this.nickname = requestDto.getNickname();
        this.mbtiType = requestDto.getMbtiType();
        this.interestType = requestDto.getInterestType();

    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

}



