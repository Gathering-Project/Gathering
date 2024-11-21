package nbc_final.gathering.domain.chatting.chatroom.entity;

import jakarta.persistence.*;
import lombok.*;
import nbc_final.gathering.domain.chatting.chatuser.entity.ChatMember;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "ChatRoom")
@DynamicUpdate
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(value = {AuditingEntityListener.class})
@AllArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_Room_id")
    private Long id;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMember> chatRoomMembers = new ArrayList<>();

    @Column(name = "createdAt", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    public void addMember(ChatMember member) {
        member.setChatRoom(this);
        this.chatRoomMembers.add(member); // ChatRoom에 ChatMember 추가
    }

    public static ChatRoom emptyChatRoom(){
        return new ChatRoom();
    }

}
