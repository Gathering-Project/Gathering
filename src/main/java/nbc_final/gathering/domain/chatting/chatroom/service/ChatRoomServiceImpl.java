package nbc_final.gathering.domain.chatting.chatroom.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.chatconfig.ChatDto;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.chatting.chatroom.entity.ChatRoom;
import nbc_final.gathering.domain.chatting.chatroom.repository.ChatRoomRepository;
import nbc_final.gathering.domain.chatting.chatuser.entity.ChatMember;
import nbc_final.gathering.domain.chatting.chatuser.repository.ChatMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;

    // 채팅방 자동 생성
    @Override
    @Transactional
    public ChatDto.ChatRoomCreateRes createChatRoomForPersonal(Long userId1, Long userId2, ChatDto.ChatRoomCreateReq chatRoomReq) {
        log.info("채팅방 생성 user1: {}, user2: {}", userId1, userId2);

        // 새로운 ChatRoom 생성
        ChatRoom newRoom = chatRoomReq.createChatRoom();
        chatRoomRepository.save(newRoom);

        // 각 유저에 대한 ChatMember 생성 및 ChatRoom에 추가
        ChatMember member1 = new ChatMember(userId1, newRoom);
        ChatMember member2 = new ChatMember(userId2, newRoom);

        chatMemberRepository.save(member1);
        chatMemberRepository.save(member2);

        // 채팅방 정보와 유저 ID 반환
        return ChatDto.ChatRoomCreateRes.createRes(newRoom.getId(), userId1, userId2);
    }


    // 특정 채팅방 조회
    @Transactional(readOnly = true)
    public ChatDto.ChatRoomDto getChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.CHAT_ROOM_NOT_FOUND));
        return new ChatDto.ChatRoomDto(chatRoom.getId(), chatRoom.getCreatedAt());
    }

    // 모든 채팅방 조회
    @Transactional(readOnly = true)
    public List<ChatDto.ChatRoomDto> getChatRooms() {
        return chatRoomRepository.findAll().stream()
                .map(chatRoom -> new ChatDto.ChatRoomDto(chatRoom.getId(), chatRoom.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
