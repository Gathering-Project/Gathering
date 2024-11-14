package nbc_final.gathering.domain.chatting.chatroom.controller;

import lombok.RequiredArgsConstructor;

import nbc_final.gathering.common.config.chatconfig.ChatDto;
import nbc_final.gathering.domain.chatting.chatroom.service.ChatRoomServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomServiceImpl chatRoomService;

    /**
     * 특정 채팅방 정보를 조회합니다.
     *
     * @param chatRoomId 조회할 채팅방의 ID
     * @return ResponseEntity에 감싸진 ChatRoomDto 객체로, 지정된 chatRoomId에 대한 채팅방의 ID와 생성 시간 등의 정보를 반환합니다.
     */
    @GetMapping("/v1/chat-rooms/{chatRoomId}")
    public ResponseEntity<ChatDto.ChatRoomDto> getChatRoom(@PathVariable Long chatRoomId) {
        return ResponseEntity.ok(chatRoomService.getChatRoom(chatRoomId));
    }

    /**
     * 모든 채팅방의 정보를 조회합니다.
     *
     * @return ResponseEntity에 감싸진 List<ChatRoomDto>로, 모든 채팅방의 ID와 생성 시간 등을 포함한 리스트를 반환합니다.
     */
    @GetMapping("/v1/chat-rooms")
    public ResponseEntity<List<ChatDto.ChatRoomDto>> getChatRooms() {
        return ResponseEntity.ok(chatRoomService.getChatRooms());
    }
}