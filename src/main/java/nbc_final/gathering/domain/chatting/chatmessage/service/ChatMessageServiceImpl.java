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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    @Autowired
    @Qualifier("mongoChatMessageRepository")
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    // 메시지를 관리할 큐 (크기는 100으로 제한)
    private final BlockingQueue<ChatMessageRes> messageQueue = new LinkedBlockingQueue<>(100);


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

        // 메시지 큐에 추가
        if (!messageQueue.offer(chatMessageRes)) {
            log.warn("Message queue is full. Dropping message: {}", chatMessageRes);
            return;
        }

        // 비동기로 메시지 처리
        CompletableFuture.runAsync(() -> {
            try {
                // 메시지 큐에서 가져오기
                ChatMessageRes messageToSend = messageQueue.take();

                // STOMP 전송 대상 경로에 전송
                messagingTemplate.convertAndSend("topic/room." + chatMessageReq.getChatRoomId(), messageToSend);
                log.info("STOMP broker로 메세지를 보냄: {}", messageToSend);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Message sending interrupted", e);
            }
        });
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