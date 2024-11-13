package nbc_final.gathering.domain.chatting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nbc_final.gathering.domain.matching.dto.response.MatchingResponseDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@Getter
@AllArgsConstructor
public class ChatMessageRequestDto {
    private String sessionId; // 채팅 세션 ID
    private Long senderId;    // 발신자 ID
    private String content;   // 메시지 내용
}