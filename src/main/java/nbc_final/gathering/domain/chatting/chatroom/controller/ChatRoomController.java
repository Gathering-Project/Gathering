package nbc_final.gathering.domain.chatroom.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.ChatDto;
import nbc_final.gathering.domain.chatroom.repository.ChatRoomRepository;
import nbc_final.gathering.domain.chatroom.service.ChatRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;

    @PostMapping("/chat-rooms")
    public ResponseEntity<ChatDto.ChatRoomCreateRes> createChatRoom(@RequestParam Long loginId,
                                                                    @RequestBody ChatDto.ChatRoomCreateReq request) {
        return ResponseEntity.ok(chatRoomService.createChatRoomForPersonal(loginId, request));
    }

    @GetMapping("/chat-rooms/{chatRoomId}")
    public ResponseEntity getChatRoom(@PathVariable Long chatRoomId) {
        return ResponseEntity.ok(chatRoomRepository.findById(chatRoomId));
    }

    @GetMapping("/chat-rooms")
    public ResponseEntity getChatRooms() {
        return ResponseEntity.ok(chatRoomRepository.findAll());
    }
}