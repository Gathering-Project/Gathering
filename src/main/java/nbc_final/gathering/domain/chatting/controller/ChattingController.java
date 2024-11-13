package nbc_final.gathering.domain.chatting.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.domain.chatting.dto.request.ChatMessageRequestDto;
import nbc_final.gathering.domain.chatting.dto.response.ChatSessionResponseDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChattingController {

    private final RabbitTemplate rabbitTemplate;

    /**
     * HTTP 요청으로 메세지를 전송합니다.
     * @param chatMessageRequestDto 세션 ID, 발신자 ID, 내용이 포함된 DTO
     */
    @PostMapping("/v1/sendMessage")
    public void sendMessage (@RequestBody ChatMessageRequestDto chatMessageRequestDto) {
        rabbitTemplate.convertAndSend("chatting.exchange", "chatting.message.routingKey", chatMessageRequestDto);
    }

    /**
     * 세션 ID를 통해 채팅 세션을 조회합니다.
     *
     * @param sessionIdString 채팅 세션의 ID
     * @return 채팅 세션 세부 정보
     */
    @GetMapping("/v1/sessions/{sessionId}")
    public ChatSessionResponseDto getChatSession(@PathVariable String sessionIdString) {
        ChatSessionResponseDto response = (ChatSessionResponseDto) rabbitTemplate.convertSendAndReceive(
                "chatting.exchange", "chatting.session.routingKey", sessionIdString);
        return response;
    }

    /**
     * 특정 메시지의 읽음 상태를 업데이트합니다.
     *
     * @param messageId 읽음 처리할 메시지의 ID
     */
    @PostMapping("/v1/messages/{messageId}/read")
    public void updateMessageReadStatus (@PathVariable String messageId) {
        rabbitTemplate.convertAndSend("chatting.exchange", "chatting.read.routingKey", messageId);
    }
}