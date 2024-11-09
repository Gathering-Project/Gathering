package nbc_final.gathering.domain.chatting.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ChatSessionResponseDto {
    private String sessionId;           // 문자열로 변환된 sessionId
    private String matchingId;          // 문자열로 변환된 matchingId
    private Long userId1;
    private Long userId2;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private List<ChatMessageResponseDto> messages;

    public ChatSessionResponseDto(String sessionId, String matchingId, Long userId1, Long userId2, LocalDateTime createdAt, LocalDateTime lastMessageAt, List<ChatMessageResponseDto> messages) {
        this.sessionId = sessionId;
        this.matchingId = matchingId;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
        this.messages = messages;
    }
}