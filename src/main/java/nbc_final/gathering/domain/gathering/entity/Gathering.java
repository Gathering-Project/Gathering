package nbc_final.gathering.domain.gathering.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gatherings",
        indexes = {
                @Index(name = "idx_gathering_title", columnList = "title"),
                @Index(name = "idx_gathering_location", columnList = "location"),
                @Index(name = "idx_gathering_title_location", columnList = "title, location")
        }
)
public class Gathering extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL)
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> Attachments = new ArrayList<>();

    @Column(length = 30, nullable = false)
    private String title;

    @Column(length = 100, nullable = false)
    private String description;

    @Column(length = 2048)
    private String gatheringImage;

    @Column(nullable = false)
    private Integer gatheringCount;

    @Column(nullable = false)
    private Integer gatheringMaxCount;

    @Column(precision = 4, scale = 1, nullable = false)
    private BigDecimal rating;

    @Column(length = 30, nullable = false)
    private String location;

    private long totalGatheringViewCount;

    private LocalDate displayDate;  // 광고 노출 희망일 필드 추가

    public Gathering(Long userId,
                     String title,
                     String description,
                     Integer gatheringCount,
                     Integer gatheringMaxCount,
                     BigDecimal rating,
                     String location) {

        this.userId = userId;
        this.title = title;
        this.description = description;
        this.gatheringCount = gatheringCount;
        this.gatheringMaxCount = gatheringMaxCount;
        this.rating = rating;
        this.location = location;
    }

    public static Gathering of(String title, int gatheringMaxCount, String description) {
        Gathering gathering = new Gathering();
        gathering.title = title;
        gathering.gatheringMaxCount = gatheringMaxCount;
        gathering.gatheringCount = 0;
        gathering.rating = BigDecimal.ZERO;
        gathering.location = "Default Location";
        gathering.description = description;
        return gathering;
    }

    public static Gathering of(Long id) {
        Gathering gathering = new Gathering();
        gathering.id = id;
        return gathering;
    }

    public void setGatheringImage(String gatheringImage) {
        this.gatheringImage = gatheringImage;
    }

    public void updateDetails(String title,
                              String description,
                              Integer gatheringMaxCount,
                              String location) {
        this.title = title;
        this.description = description;
        this.gatheringMaxCount = gatheringMaxCount;
        this.location = location;
    }

    public void addMember(User user, MemberRole role, MemberStatus status) {
        Member newMember = new Member(user, this, role, status);
        this.members.add(newMember);
    }

    public void updateTotalGatheirngViewCount(long totalGatheringViewCount) {
        this.totalGatheringViewCount = totalGatheringViewCount;
    }

    public void updateGatheirngCount(Integer gatheringCount) {
        this.gatheringCount = gatheringCount;
    }
}
