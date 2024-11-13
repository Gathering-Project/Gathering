package nbc_final.gathering.domain.chatmessage.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.ChatDto;
import nbc_final.gathering.common.ChatMessageRes;
import nbc_final.gathering.domain.chatmessage.service.ChatMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * Destination Queue: /pub/chat.message를 통해 호출 후 처리 되는 로직
     */
    @MessageMapping("chat.message")
    public ResponseEntity sendMessage(ChatDto.ChatMessageReq message) {
        chatMessageService.sendMessage(message);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chat-messages/chat-room/{chatRoomId}")
    public ResponseEntity getChatMessages(@PathVariable Long chatRoomId) {
        List<ChatMessageRes> chatMessageResList = chatMessageService.getChatMessagesByChatRoomId(chatRoomId);
        return ResponseEntity.ok(chatMessageResList);
    }
}