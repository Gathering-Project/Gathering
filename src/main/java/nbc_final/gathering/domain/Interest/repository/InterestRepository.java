package nbc_final.gathering.domain.Interest.repository;

import nbc_final.gathering.domain.Interest.entity.Interest;
import nbc_final.gathering.domain.user.enums.InterestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Integer> {


  Optional<Interest> findByInterestType(InterestType interestType);
}
