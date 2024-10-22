package nbc_final.gathering.domain.member.repository;

import nbc_final.gathering.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
