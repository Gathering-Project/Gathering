package nbc_final.gathering.domain.chatting.chatuser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.chatting.chatroom.entity.ChatRoom;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CHATMEMBER")
@Table(name = "chat_member")
public class ChatMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_member_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    // 유저 ID와 ChatRoom 객체를 받아서 ChatMember 객체 생성
    public ChatMember(Long userId, ChatRoom chatRoom) {
        this.userId = userId;
        this.chatRoom = chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}