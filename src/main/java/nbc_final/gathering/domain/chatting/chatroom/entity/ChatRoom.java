package nbc_final.gathering.domain.chatroom.entity;

import jakarta.persistence.*;
import lombok.*;
import nbc_final.gathering.domain.user.entity.Member;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "ChatRoom")
@DynamicUpdate
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(value = {AuditingEntityListener.class})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_Room_id")
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "Chat_Room_Members",
            joinColumns = @JoinColumn(name = "chatRoomId"),
            inverseJoinColumns = @JoinColumn(name = "memberId"))
    private Set<Member> chatRoomMembers = new HashSet<>();

    @Column(name = "createdAt", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    public void addMembers(Member roomMaker, Member guest) {
        this.chatRoomMembers.add(roomMaker);
        this.chatRoomMembers.add(guest);
    }

    public static ChatRoom emptyChatRoom(){
        return new ChatRoom();
    }

}
