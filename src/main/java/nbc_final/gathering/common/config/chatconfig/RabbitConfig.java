package nbc_final.gathering.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class RabbitConfig {

    @Value("${rabbitmq.chat.queue.name}")
    private String chatQueueName;
    @Value("${rabbitmq.chat.exchange.name}")
    private String chatExchangeName;
    @Value("${rabbitmq.chat.routing.key}")
    private String routingKey;

    @Value("${rabbitmq.host}")
    private String host;
    @Value("${rabbitmq.port}")
    private int port;
    @Value("${rabbitmq.virtual-host}")
    private String virtualHost;
    @Value("${rabbitmq.username}")
    private String username;
    @Value("${rabbitmq.password}")
    private String password;

    // Queue 등록
    @Bean
    public Queue queue() {
        return new Queue(chatQueueName, true);
    }

    // Exchange 등록
    @Bean
    public TopicExchange exchange() {
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

    // RabbitMQ와의 메시지 통신을 담당하는 클래스
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());

        rabbitTemplate.setExchange(chatExchangeName);
        return rabbitTemplate;
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

    // 메시지를 JSON형식으로 직렬화하고 역직렬화하는데 사용되는 변환기
    // RabbitMQ 메시지를 JSON형식으로 보내고 받을 수 있음
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        //LocalDateTime serializable을 위해
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.registerModule(dateTimeModule());

        return new dateTimeModule;
    }

    @Bean
    public Module dateTimeModule() {
        return new JavaTimeModule();
    }
}