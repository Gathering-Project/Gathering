package nbc_final.gathering.domain.chatting.chatroom.service;


import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.config.chatconfig.ChatDto;
import nbc_final.gathering.domain.chatting.chatroom.entity.ChatRoom;
import nbc_final.gathering.domain.chatting.chatroom.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatDto.ChatRoomCreateRes createChatRoomForPersonal(Long loginId, ChatDto.ChatRoomCreateReq chatRoomReq) {
        ChatRoom roomMaker = memberRepository.findById(loginId)
                .orElseThrow(() -> new RuntimeException("MemberNotFoundException"));
        ChatRoom guest = memberRepository.findById(chatRoomReq.getGuestId())
                .orElseThrow(() -> new RuntimeException("UserNotFoundException"));

        ChatRoom newRoom = chatRoomReq.createChatRoom();
        newRoom.addMembers(roomMaker, guest);
        chatRoomRepository.save(newRoom);

        return ChatDto.ChatRoomCreateRes.createRes(newRoom.getId(), roomMaker.getId(), guest.getId());
    }
}