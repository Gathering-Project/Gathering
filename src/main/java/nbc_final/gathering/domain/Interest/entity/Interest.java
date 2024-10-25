package nbc_final.gathering.domain.Interest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.userInterest.entity.UserInterest;
import nbc_final.gathering.domain.userInterest.entity.UserInterestId;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "interests")
public class Interest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private InterestType interestType;

  @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UserInterest> userInterests = new HashSet<UserInterest>();

  public Interest(InterestType interestType) {
    this.interestType = interestType;
  }

  public void addInterest(InterestType interestType) {
    this.interestType = interestType;
  }

  // 복합키 생성 및 관심사 유저 추가 메서드
  public void addUserInterest(User user) {
    UserInterestId userInterestId = new UserInterestId(user.getId(), this.id);
    UserInterest userInterest = new UserInterest(userInterestId, user, this);
    this.userInterests.add(userInterest);
  }
}
