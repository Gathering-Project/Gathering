package nbc_final.gathering.common.rabbitmq.customer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQAlarmConsumer {

    @RabbitListener(queues = "user-notification-queue")
    public void listenUserNotification(String message) {
        // 유저 알림 처리 로직
        System.out.println("User Notification Received: " + message);
    }

    @RabbitListener(queues = "member-join-request-queue")
    public void listenMemberJoinRequest(String message) {
        // 호스트 알림 처리 로직
        System.out.println("Member Join Request Received: " + message);
    }
}
