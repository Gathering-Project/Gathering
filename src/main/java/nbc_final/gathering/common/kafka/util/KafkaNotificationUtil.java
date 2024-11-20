package nbc_final.gathering.common.kafka.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNotificationUtil {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void notifyUser(Long userId, Object message) {
        kafkaTemplate.send("user-notification-topic", userId.toString(), message);
        log.info("Kafka 알림 전송: 유저 ID={}, 메시지={}", userId, message);
    }

    public void notifyGuestMember(Long memberId, Object message) {
        kafkaTemplate.send("guest-member-notification-topic", memberId.toString(), message);
        log.info("Kafka 알림 전송: 게스트 멤버 ID={}, 메시지={}", memberId, message);
    }

    public void notifyHostMember(Long memberId, Object message) {
        kafkaTemplate.send("host-member-notification-topic", memberId.toString(), message);
        log.info("Kafka 알림 전송: 호스트 멤버 ID={}, 메시지={}", memberId, message);
    }

    public void notifyAllMembers(Long gatheringId, Object message) {
        kafkaTemplate.send("gathering-updated-topic", gatheringId.toString(), message);
        log.info("Kafka 알림 전송: 소모임 ID={}, 메시지={}", gatheringId, message);
    }

    public void notifyMember(Long userId, Object message) {
        String topic = "user-notification-topic";
        kafkaTemplate.send(topic, userId.toString(), message);
        log.info("특정 멤버 알림 전송 완료: userId={}, message={}", userId, message);
    }
}
