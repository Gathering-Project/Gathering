package nbc_final.gathering.domain.chatmessage.service;

import nbc_final.gathering.common.ChatDto;
import nbc_final.gathering.common.ChatMessageRes;

import java.util.List;

public interface ChatMessageService {
    void sendMessage(ChatDto.ChatMessageReq message);

    List<ChatMessageRes> getChatMessagesByChatRoomId(Long chatRoomId);
}
