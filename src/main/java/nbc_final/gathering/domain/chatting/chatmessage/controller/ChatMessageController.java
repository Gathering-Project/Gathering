package nbc_final.gathering.domain.chatting.chatmessage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import nbc_final.gathering.common.config.chatconfig.ChatDto;
import nbc_final.gathering.common.config.chatconfig.ChatMessageRes;
import nbc_final.gathering.domain.chatting.chatmessage.service.ChatMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "ChatMessage API", description = "채팅 메세지 관련 API 모음입니다.")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * 클라이언트가 /pub/chat.message로 메시지를 보낼 때 호출되어 처리되는 메서드입니다.
     *
     * @param message 클라이언트로부터 전달된 메시지 요청 정보
     * @return ResponseEntity.ok() 성공적으로 메시지가 전송되었음을 나타내는 빈 응답을 반환합니다.
     */
    @MessageMapping("chat.message")
    public ResponseEntity sendMessage(ChatDto.ChatMessageReq message) {
        chatMessageService.sendMessage(message);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 채팅방의 모든 메시지를 조회하는 메서드입니다.
     *
     * @param chatRoomId 조회할 채팅방의 ID
     * @return ResponseEntity에 감싸진 List<ChatMessageRes>로, 지정된 채팅방 ID의 모든 메시지 정보를 반환합니다.
     */
    @Operation(summary = "특정 채팅방 메세지 조회", description = "특정 채팅방의 모든 메시지를 조회합니다.")
    @GetMapping("/api/v1/chat-messages/chat-room/{chatRoomId}")
    public ResponseEntity getChatMessages(@PathVariable Long chatRoomId) {
        List<ChatMessageRes> chatMessageResList = chatMessageService.getChatMessagesByChatRoomId(chatRoomId);
        return ResponseEntity.ok(chatMessageResList);
    }
}