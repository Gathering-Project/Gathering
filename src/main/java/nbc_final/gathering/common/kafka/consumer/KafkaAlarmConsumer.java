//package nbc_final.gathering.common.kafka.consumer;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import nbc_final.gathering.common.kafka.util.KafkaNotificationUtil;
//import nbc_final.gathering.domain.matching.dto.response.MatchingResponseDto;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class KafkaAlarmConsumer {
//
//    private final KafkaNotificationUtil kafkaNotificationUtil;
//
//    @KafkaListener(topics = "user-notification-topic", groupId = "notification-group")
//    public void listenUserLogin(String message) {
//        log.info(message);
//        sendNotificationToUser(message);
//    }
//
//    @KafkaListener(topics = "user-logout-topic", groupId = "notification-group")
//    public void listenUserLogout(String message) {
//        log.info(message);
//        sendNotificationToUser(message);
//    }
//
//    @KafkaListener(topics = "user-info-updated-topic", groupId = "notification-group")
//    public void listenUserInfoUpdated(String message) {
//        log.info(message);
//        sendNotificationToUser(message);
//    }
//
//    @KafkaListener(topics = "member-join-request-topic", groupId = "notification-group")
//    public void listenMemberJoinRequest(String message) {
//        log.info(message);
//        sendNotificationToHost(message);
//    }
//
//    // 매칭 결과 알림
//    @KafkaListener(topics = "matching-to-notification", groupId = "notification-group")
//    public void handleMatchingResult(ConsumerRecord<String, Object> request) {
//
//        Object result = request.value(); // 메세지 값 추출
//
//        if (result instanceof MatchingResponseDto) {
//            MatchingResponseDto response = (MatchingResponseDto) result;
//
//            String message1 = String.format("ID %d번 유저와 매칭에 성공하였습니다.", response.getUserId2());
//            String message2 = String.format("ID %d번 유저와 매칭에 성공하였습니다.", response.getUserId1());
//
//            kafkaNotificationUtil.notifyUser(response.getUserId1(), message1);
//            kafkaNotificationUtil.notifyUser(response.getUserId2(), message2);
//            log.info("ID {}번, {}번 유저에게 매칭 성공 알림 전송 완료", response.getUserId1(), response.getUserId2());
//
//        } else if (result instanceof Long) {
//            Long response = (Long) result;
//
//            String message = "거주 지역과 관심사가 일치하는 유저가 존재하지 않아 매칭에 실패하였습니다.";
//            kafkaNotificationUtil.notifyUser(response, "거주 지역과 관심사가 일치하는 유저가 존재하지 않아 매칭에 실패하였습니다.");
//            log.info("ID {}번 유저에게 매칭 실패 알림 전송 완료", response);
//        }
//    }
//
//    private void sendNotificationToUser(String message) {
//        // 유저에게 알림 전송 (Slack, WebSocket 등)
//    }
//
//    private void sendNotificationToHost(String message) {
//        // 호스트에게 알림 전송
//    }
//}
