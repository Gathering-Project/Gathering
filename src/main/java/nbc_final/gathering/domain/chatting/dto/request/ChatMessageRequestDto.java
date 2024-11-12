package nbc_final.gathering.domain.chatmessage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageRequestDto {
    private String sessionId; // 채팅 세션 ID
    private Long senderId;    // 발신자 ID
    private String content;   // 메시지 내용
}