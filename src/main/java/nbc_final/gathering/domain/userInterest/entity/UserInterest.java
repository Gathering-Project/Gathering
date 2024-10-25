package nbc_final.gathering.domain.userInterest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.Interest.entity.Interest;
import nbc_final.gathering.domain.user.entity.User;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "user_interest")
public class UserInterest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private UserInterestId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("user")
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("interest")
  @JoinColumn(name = "interest_id")
  private Interest interest;

}