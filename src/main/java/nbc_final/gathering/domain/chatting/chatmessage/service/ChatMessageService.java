package nbc_final.gathering.domain.chatting.chatmessage.service;


import nbc_final.gathering.common.config.chatconfig.ChatDto;
import nbc_final.gathering.common.config.chatconfig.ChatMessageRes;

import java.util.List;

public interface ChatMessageService {
    void sendMessage(ChatDto.ChatMessageReq message);

    List<ChatMessageRes> getChatMessagesByChatRoomId(Long chatRoomId);
}
