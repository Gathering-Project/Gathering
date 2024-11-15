package nbc_final.gathering.common.rabbitmq.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQAlarmProducer {

    private final RabbitTemplate rabbitTemplate;

    public void notifyUser(Long userId, String message) {
        rabbitTemplate.convertAndSend("user-notification-queue", message);
    }

    public void notifyHost(Long memberId, String message) {
        rabbitTemplate.convertAndSend("member-join-request-queue", message);
    }
}
