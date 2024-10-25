package nbc_final.gathering.domain.userInterest.entity;


import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserInterestId implements Serializable {

  private Long user;
  private Long interest;

  public UserInterestId() {}

  public UserInterestId(Long user, Long interest) {
    this.user = user;
    this.interest = interest;
  }

  // equals() 및 hashCode() 메서드 구현
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserInterestId)) return false;
    UserInterestId that = (UserInterestId) o;
    return Objects.equals(user, that.user) && Objects.equals(interest, that.interest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, interest);
  }
}
