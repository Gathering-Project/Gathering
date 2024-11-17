package nbc_final.gathering.domain.chatting.chatuser.repository;

import nbc_final.gathering.domain.chatting.chatuser.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
}
