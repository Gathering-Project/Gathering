package nbc_final.gathering.domain.chatting.chatmessage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.chatconfig.ChatDto;
import nbc_final.gathering.common.config.chatconfig.ChatMessageRes;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.chatting.chatmessage.entity.ChatMessage;
import nbc_final.gathering.domain.chatting.chatmessage.repository.ChatMessageRepository;
import nbc_final.gathering.domain.chatting.chatroom.entity.ChatRoom;
import nbc_final.gathering.domain.chatting.chatroom.repository.ChatRoomRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅 send 메서드
    @Override
    public void sendMessage(ChatDto.ChatMessageReq chatMessageReq) {
        // ChatRoom이 존재하는지 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageReq.getChatRoomId())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.CHAT_ROOM_NOT_FOUND));

        // User가 존재하는지 확인
        User user = userRepository.findById(chatMessageReq.getMemberId())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

        // ChatMessage 엔티티 생성 및 저장
        ChatMessage chatMessage = chatMessageReq.toEntity(chatRoom.getId(), user.getId());
        chatMessageRepository.save(chatMessage);  // MongoDB에 저장

        // 전송 메시지 생성
        ChatMessageRes chatMessageRes = ChatMessageRes.createRes(chatMessage);

        // STOMP 전송 대상 경로에 전송
        messagingTemplate.convertAndSend("topic/room." + chatMessageReq.getChatRoomId(), chatMessageRes);
        log.info("STOMP broker로 메세지를 보냄: {}", chatMessage);
    }

    // 채팅방에서 채팅내용 찾는 메서드
    @Override
    public List<ChatMessageRes> getChatMessagesByChatRoomId(Long chatRoomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
        return chatMessages.stream()
                .map(ChatMessageRes::createRes)
                .toList();
    }
}