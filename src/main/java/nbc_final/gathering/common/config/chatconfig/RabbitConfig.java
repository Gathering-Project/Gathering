    package nbc_final.gathering.common.config.chatconfig;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.databind.SerializationFeature;
    import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
    import lombok.RequiredArgsConstructor;
    import org.springframework.amqp.core.*;
    import org.springframework.amqp.rabbit.annotation.EnableRabbit;
    import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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
        // 알람 메시지를 처리할 큐와 바인딩 추가
        @Value("${RABBITMQ_NOTIFICATION_QUEUE}")
        private String notificationQueueName;
        @Value("${RABBITMQ_NOTIFICATION_EXCHANGE}")
        private String notificationExchangeName;

        @Value("${rabbitmq_host}")
        private String host;
        @Value("${rabbitmq_port}")
        private int port;
        @Value("${rabbitmq_virtual_host}")
        private String virtualHost;
        @Value("${rabbitmq_username}")
        private String username;
        @Value("${rabbitmq_password}")
        private String password;

        // 알람 큐 설정
        @Bean
        public Queue notificationQueue() {
            return new Queue(notificationQueueName, true);
        }

        // 알람 메시지를 처리할 Direct Exchange 설정
        @Bean
        public DirectExchange notificationExchange() {
            return new DirectExchange(notificationExchangeName);
        }

        // 알람 큐를 알람 교환기와 바인딩
        @Bean
        public Binding notificationBinding() {
            return BindingBuilder
                    .bind(notificationQueue())
                    .to(notificationExchange())
                    .with("user-notification-routing-key");
        }

        // 채팅 메시지를 처리할 durable(내구성 있는) 큐를 정의
        @Bean
        public Queue queue() {
            return new Queue("chat.queue", true);
        }

        // 매칭 성공 이벤트를 처리할 durable 큐를 정의
        @Bean
        public Queue matchingSuccessQueue() {
            return new Queue("matching.success", true);
        }

        // 매칭 실패 이벤트를 처리할 durable 큐를 정의
        @Bean
        public Queue matchingFailedQueue() {
            return new Queue("matching.failed", true);
        }

        // 매칭 관련 메시지를 처리할 Direct Exchange를 정의
        @Bean
        public DirectExchange matchingExchange() {
            return new DirectExchange("matching.exchange");
        }

        // 성공 큐를 매칭 익스체인지에 특정 라우팅 키로 바인딩
        @Bean
        public Binding matchingSuccessBinding() {
            return BindingBuilder
                    .bind(matchingSuccessQueue())
                    .to(matchingExchange())
                    .with("matching.success");
        }

        // 실패 큐를 매칭 익스체인지에 특정 라우팅 키로 바인딩
        @Bean
        public Binding matchingFailedBinding() {
            return BindingBuilder
                    .bind(matchingFailedQueue())
                    .to(matchingExchange())
                    .with("matching.failed");
        }

        // 채팅 관련 메시지를 처리할 Topic Exchange를 정의
        @Bean
        public TopicExchange exchange() {
            return new TopicExchange(chatExchangeName);
        }

        // 채팅 큐를 Topic Exchange에 특정 라우팅 키로 바인딩
        @Bean
        public Binding binding(Queue queue, TopicExchange exchange) {
            return BindingBuilder
                    .bind(queue)
                    .to(exchange)
                    .with(routingKey);
        }

        // 매칭 관련 메시지를 처리할 Direct Exchange를 정의
        @Bean
        public DirectExchange directExchange() {
            return new DirectExchange("matching.exchange");
        }

        // RabbitTemplate을 구성하여 메시지 발송 처리
        @Bean
        public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                             MessageConverter messageConverter) {
            var rabbitTemplate = new RabbitTemplate(connectionFactory);
            rabbitTemplate.setMessageConverter(messageConverter); // 커스텀 메시지 변환기 설정
            rabbitTemplate.setExchange(chatExchangeName); // 기본 익스체인지 설정
            rabbitTemplate.setMessageConverter(jsonMessageConverter()); // JSON 직렬화 보장
            return rabbitTemplate;
        }

        // 메시지를 JSON 형식으로 직렬화/역직렬화하는 메시지 변환기를 설정
        @Bean
        public MessageConverter messageConverter(ObjectMapper objectMapper) {
            return new Jackson2JsonMessageConverter(objectMapper);
        }

        // LocalDateTime 직렬화를 처리할 Jackson2JsonMessageConverter를 정의
        @Bean
        public Jackson2JsonMessageConverter jsonMessageConverter() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true); // 날짜를 타임스탬프로 사용
            Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
            return converter;
        }

        // RabbitMQ 리스너 관리를 위한 팩토리를 구성
        @Bean
        public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
            SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setAcknowledgeMode(AcknowledgeMode.AUTO); // 메시지 자동 인식 설정
            factory.setPrefetchCount(10); // QoS: 한 번에 가져올 메시지 수 제한
            return factory;
        }

        // RabbitMQ 연결 관리를 위한 팩토리를 구성
        @Bean
        public ConnectionFactory connectionFactory() {
            CachingConnectionFactory factory = new CachingConnectionFactory();
            factory.setHost(host); // RabbitMQ 호스트 설정
            factory.setPort(port); // RabbitMQ 포트 설정
            factory.setVirtualHost(virtualHost); // RabbitMQ 가상 호스트 설정
            factory.setUsername(username); // RabbitMQ 사용자 이름 설정
            factory.setPassword(password); // RabbitMQ 비밀번호 설정
            return factory;
        }
    }