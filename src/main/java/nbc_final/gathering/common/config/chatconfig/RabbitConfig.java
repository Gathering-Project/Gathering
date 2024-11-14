package nbc_final.gathering.common.config.chatconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class RabbitConfig {

    @Value("${rabbitmq_chat_queue_name}")
    private String chatQueueName;
    @Value("${rabbitmq_chat_exchange_name}")
    private String chatExchangeName;
    @Value("${rabbitmq_chat_routing_key}")
    private String routingKey;

    @Value("${rabbitmq_host}")
    private String host;
    @Value("${rabbitmq_port}")
    private int port;
    @Value("${rabbitmq_virtual-host}")
    private String virtualHost;
    @Value("${rabbitmq_username}")
    private String username;
    @Value("${rabbitmq_password}")
    private String password;

    // Queue 등록
    @Bean
    public Queue queue() {
        return new Queue("chat.queue", true);
    }

    @Bean
    public Queue matchingSuccessQueue() {
        return new Queue("matching.success", true); // durable을 원하면 true로 설정
    }

    @Bean
    public Queue matchingFailedQueue() {
        return new Queue("matching.failed", true); // durable을 true로 설정하려면 true로 변경하세요
    }

    // Matching Exchange 등록
    @Bean
    public DirectExchange matchingExchange() {
        return new DirectExchange("matching.exchange");
    }

    // Matching Exchange와 각 Queue 바인딩
    @Bean
    public Binding matchingSuccessBinding() {
        return BindingBuilder
                .bind(matchingSuccessQueue())
                .to(matchingExchange())
                .with("matching.success");
    }

    @Bean
    public Binding matchingFailedBinding() {
        return BindingBuilder
                .bind(matchingFailedQueue())
                .to(matchingExchange())
                .with("matching.failed");
    }

    // Exchange 등록
    @Bean
    public TopicExchange exchange()  {
        return new TopicExchange(chatExchangeName);
    }

    // Exchange와 Queue바인딩
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange){
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(routingKey);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("matching.exchange");
    }

    // RabbitMQ와의 메시지 통신을 담당하는 클래스
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setExchange(chatExchangeName);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        //LocalDateTime serializable을 위해
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
//        objectMapper.registerModule(dateTimeModule());

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        return converter;
    }


    // RabbitMQ와의 연결을 관리하는 클래스
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setVirtualHost(virtualHost);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }
}