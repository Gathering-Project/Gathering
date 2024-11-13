package nbc_final.gathering.domain.chatting.user.repository;


import nbc_final.gathering.domain.chatting.user.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
}