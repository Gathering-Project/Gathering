package nbc_final.gathering.domain.chatroom.service;

import nbc_final.gathering.common.ChatDto;

public interface ChatRoomService {
    ChatDto.ChatRoomCreateRes createChatRoomForPersonal(Long id, ChatDto.ChatRoomCreateReq request);
}