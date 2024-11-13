package nbc_final.gathering.domain.chatting.chatroom.service;


import nbc_final.gathering.common.config.chatconfig.ChatDto;

public interface ChatRoomService {
    ChatDto.ChatRoomCreateRes createChatRoomForPersonal(Long id, ChatDto.ChatRoomCreateReq request);
}