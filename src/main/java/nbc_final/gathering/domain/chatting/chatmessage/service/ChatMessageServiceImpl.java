package nbc_final.gathering.domain.chatmessage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.ChatDto;
import nbc_final.gathering.common.ChatMessageRes;
import nbc_final.gathering.domain.chatmessage.entity.ChatMessage;
import nbc_final.gathering.domain.chatmessage.repository.ChatMessageRepository;
import nbc_final.gathering.domain.chatroom.entity.ChatRoom;
import nbc_final.gathering.domain.chatroom.repository.ChatRoomRepository;
import nbc_final.gathering.domain.user.entity.Member;
import nbc_final.gathering.domain.user.repository.MemberRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(ChatDto.ChatMessageReq chatMessageReq) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageReq.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        Member member = memberRepository.findById(chatMessageReq.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        ChatMessage chatMessage = chatMessageReq.toEntity(chatRoom.getId(), member.getId());
        chatMessageRepository.save(chatMessage);

        ChatMessageRes chatMessageRes = ChatMessageRes.createRes(chatMessage);

        rabbitTemplate.convertAndSend("room." + chatMessageReq.getChatRoomId(), chatMessageRes);
        log.info("Message sent to RabbitMQ: {}", chatMessage);
    }

    @Override
    public List<ChatMessageRes> getChatMessagesByChatRoomId(Long chatRoomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
        return chatMessages.stream()
                .map(ChatMessageRes::createRes)
                .toList();
    }
}