package nbc_final.gathering.domain.chatting.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChatMessageResponseDto {
    private String messageId;      // ObjectId 대신 String 사용
    private Long senderId;
    private String content;
    private LocalDateTime sentAt;

    public ChatMessageResponseDto(String messageId, Long senderId, String content, LocalDateTime sentAt) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = sentAt;
    }
}