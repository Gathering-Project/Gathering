//package nbc_final.gathering.common.rabbitmq;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import nbc_final.gathering.common.config.chatconfig.ChatDto;
//import nbc_final.gathering.domain.chatting.chatroom.entity.ChatRoom;
//import nbc_final.gathering.domain.chatting.chatroom.service.ChatRoomServiceImpl;
//import nbc_final.gathering.domain.chatting.chatuser.entity.ChatMember;
//import nbc_final.gathering.domain.chatting.chatuser.repository.ChatMemberRepository;
//import nbc_final.gathering.domain.matching.dto.response.MatchingFailed;
//import nbc_final.gathering.domain.matching.dto.response.MatchingSuccess;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class RabbitMqConsumeService {
//
//    private final ChatRoomServiceImpl chatRoomServiceImpl;
//
//    @RabbitListener(queues = "matching.success")
//    @Transactional
//    public void successMatchingConsumer(MatchingSuccess matchingSuccess) {
//
//        Long userId1 = matchingSuccess.getUserId1();
//        Long userId2 = matchingSuccess.getUserId2();
//
//        log.info("Received matching success event for user IDs: {} and {}", userId1, userId2);
//
//        try {
//            // ChatRoom 생성
//            ChatDto.ChatRoomCreateReq chatRoomReq = new ChatDto.ChatRoomCreateReq(userId1, userId2);
//            ChatDto.ChatRoomCreateRes chatRoomRes = chatRoomServiceImpl.createChatRoomForPersonal(userId1, userId2, chatRoomReq);
//
//            log.info("채팅방 생성 성공 - 채팅방 ID: {}", chatRoomRes.getChatRoomId());;
//
//
//        } catch (Exception e) {
//            log.error("채팅방 생성 중 오류 발생: 유저 ID {}와 유저 ID {}, 오류 메시지: {}", userId1, userId2, e.getMessage());
//            e.printStackTrace();
//            // 예외 발생 시 추가 처리 로직 (필요 시 재시도, 알림 등)
//        }
//    }
//
//    @RabbitListener(queues = "matching.failed")
//    public void failedMatchingConsumer(MatchingFailed matchingFailed) {
//        log.info("유저 ID {} 매칭 실패", matchingFailed.getFailedUserId());
//    }
//
//}
