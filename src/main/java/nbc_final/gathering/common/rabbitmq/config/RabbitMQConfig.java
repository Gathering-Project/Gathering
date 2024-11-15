package nbc_final.gathering.common.rabbitmq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue userNotificationQueue() {
        return new Queue("user-notification-queue", true);
    }

    @Bean
    public Queue memberJoinRequestQueue() {
        return new Queue("member-join-request-queue", true);
    }
}
