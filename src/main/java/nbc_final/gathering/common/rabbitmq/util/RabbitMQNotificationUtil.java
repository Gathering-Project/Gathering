package nbc_final.gathering.common.rabbitmq.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQNotificationUtil {

    private final RabbitTemplate rabbitTemplate;

    public void notifyUser(Long userId, String message) {
        rabbitTemplate.convertAndSend("user-notification-queue", message);
        log.info("RabbitMQ 알림 전송: 유저 ID={}, 메시지={}", userId, message);
    }

    public void notifyGuestMember(Long memberId, String message) {
        rabbitTemplate.convertAndSend("guest-member-notification-queue", message);
        log.info("RabbitMQ 알림 전송: 게스트 멤버 ID={}, 메시지={}", memberId, message);
    }

    public void notifyHostMember(Long memberId, String message) {
        rabbitTemplate.convertAndSend("host-member-notification-queue", message);
        log.info("RabbitMQ 알림 전송: 호스트 멤버 ID={}, 메시지={}", memberId, message);
    }

    public void notifyAllMembers(Long gatheringId, String message) {
        rabbitTemplate.convertAndSend("gathering-updated-queue", message);
        log.info("RabbitMQ 알림 전송: 소모임 ID={}, 메시지={}", gatheringId, message);
    }

    public void notifyMember(Long userId, String message) {
        rabbitTemplate.convertAndSend("user-notification-queue", message);
        log.info("특정 멤버 알림 전송 완료: userId={}, message={}", userId, message);
    }
}
