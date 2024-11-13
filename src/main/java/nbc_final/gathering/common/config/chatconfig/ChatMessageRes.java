package nbc_final.gathering.common;

import lombok.Builder;
import lombok.Getter;
import nbc_final.gathering.domain.chatmessage.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageRes {
    private Long chatRoomId;
    private Long memberId;
    private String message;
    private LocalDateTime createdAt;

    public static ChatMessageRes createRes(ChatMessage chatMessage) {
        return ChatMessageRes.builder()
                .chatRoomId(chatMessage.getChatRoomId())
                .memberId(chatMessage.getMemberId())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}