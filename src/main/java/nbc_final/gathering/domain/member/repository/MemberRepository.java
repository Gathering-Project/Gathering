package nbc_final.gathering.domain.member.repository;

import nbc_final.gathering.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
  List<Member> findByUserId(Long userId);

}
