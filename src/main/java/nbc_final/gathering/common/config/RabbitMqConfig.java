package nbc_final.gathering.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
//    public static final String QUEUE_NAME = "matching.queue";
//
//    @Bean
//    public DirectExchange directExchange() {
//        return new DirectExchange("matching.exchange");
//    }
//
//    @Bean
//    public Queue queue() {
//        return new Queue(QUEUE_NAME, true); // durable queue
//    }
//    @Bean
//    public Binding binding(DirectExchange directExchange, Queue queue) {
//        return BindingBuilder.bind(queue).to(directExchange).with("matching.key");
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(jsonMessageConverter());
//
//        return rabbitTemplate;
//    }
//
//    @Bean
//    public Jackson2JsonMessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    @Bean
//    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
//                                                    MessageListenerAdapter listenerAdapter) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.setQueueNames(QUEUE_NAME);
//        container.setMessageListener(listenerAdapter);
//        return container;
//    }
//    // 리스너 어댑터 빈을 정의
//    @Bean
//    public MessageListenerAdapter listenerAdapter(MatchingReceiver receiver) {
//        return new MessageListenerAdapter(receiver, "receiveMessage");
//    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("matching.exchange");
    }

    @Bean
    public Queue queue() {
        return new Queue("matching.queue");
    }

    @Bean
    public Binding binding(DirectExchange directExchange, Queue queue) {
        return BindingBuilder.bind(queue).to(directExchange).with("matching.key");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }


}
