package nbc_final.gathering.domain.chatting.chatmessage.entity;

import jakarta.persistence.Column;
import lombok.*;
import nbc_final.gathering.domain.chatting.chatroom.entity.ChatRoom;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_message")
@ToString
public class ChatMessage {

    @Id
    private String id;

    private Long chatRoomId;

    private Long memberId;

    private String message;

    @CreatedDate
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;
}
