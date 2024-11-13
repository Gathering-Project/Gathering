package nbc_final.gathering.config;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageReceiver {

    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "chat.queue") // RabbitMQ에서 지정한 큐 이름
    public void receiveMessage(String message) {
        // 수신한 메세지 처리
        System.out.println("받은 메세지:" + message);

        messagingTemplate.convertAndSend("/topic/messages", message);
    }
}