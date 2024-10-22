package nbc_final.gathering.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.user.entity.User;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "members")
public class Member extends TimeStamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gathering_id", nullable = false)
  private Gathering gathering;

  @Enumerated(EnumType.STRING) // 사용자 역할을 문자열로 저장
  private MemberRole role;

  @Enumerated(EnumType.STRING)
  private MemberStatus status;

  public Member(User user, Gathering gathering, MemberRole role, MemberStatus status) {
    this.user = user;
    this.gathering = gathering;
    this.role = role;
    this.status = MemberStatus.PENDING;
  }

  public void approve() {
    this.status = MemberStatus.APPROVED;
  }

  public void setRole(MemberRole role) {
    this.role = role;
  }
}
