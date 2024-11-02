package nbc_final.gathering.common.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaAlarmConsumer {

    @KafkaListener(topics = "user-notification-topic", groupId = "notification-group")
    public void listenUserLogin(String message) {
        log.info(message);
        sendNotificationToUser(message);
    }

    @KafkaListener(topics = "user-logout-topic", groupId = "notification-group")
    public void listenUserLogout(String message) {
        log.info(message);
        sendNotificationToUser(message);
    }

    @KafkaListener(topics = "user-info-updated-topic", groupId = "notification-group")
    public void listenUserInfoUpdated(String message) {
        log.info(message);
        sendNotificationToUser(message);
    }

    @KafkaListener(topics = "member-join-request-topic", groupId = "notification-group")
    public void listenMemberJoinRequest(String message) {
        log.info(message);
        sendNotificationToHost(message);
    }

    private void sendNotificationToUser(String message) {
        // 유저에게 알림 전송 (Slack, WebSocket 등)
    }

    private void sendNotificationToHost(String message) {
        // 호스트에게 알림 전송
    }
}