package nbc_final.gathering.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.Interest.entity.Interest;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.user.dto.request.UserUpdateRequestDto;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.user.enums.MbtiType;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.userInterest.entity.UserInterest;
import nbc_final.gathering.domain.userInterest.entity.UserInterestId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(unique = true)
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

    // 이미지 파일 경로
    private String profileImagePath;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserInterest> userInterests = new HashSet<>();


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

    public void setRandomNickname(String randomNickname) {
        this.nickname = randomNickname;
    }


    // 복합키 생성 및 관심사 추가 메서드
    public void addUserInterest(Interest interest) {
        UserInterestId userInterestId = new UserInterestId(this.id, interest.getId());
        UserInterest userInterest = new UserInterest(userInterestId, this, interest);
        this.userInterests.add(userInterest);
    }
    // 관심사 삭제 메서드
    public void removeUserInterest(Interest interest) {
        this.userInterests.removeIf(ui -> ui.getInterest().equals(interest));
    }





}



