package nbc_final.gathering.common.alarmconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private static final String NOTIFICATION_EXCHANGE = "notification-exchange";
    private static final String ROUTING_KEY = "user-notification-routing-key";
    private final RabbitTemplate rabbitTemplate;
    private final AlarmMessageRepository alarmMessageRepository;

    // 알람 메시지를 RabbitMQ로 전송하는 서비스 메서드
    public void sendAlarm(AlarmDto.AlarmMessageReq request) {
        // 메시지 엔티티 생성 및 저장
        AlarmMessage alarmMessage = request.toEntity();
        alarmMessageRepository.save(alarmMessage);

        // RabbitMQ로 전송
        AlarmDto.AlarmMessageRes alarmMessageRes = AlarmDto.AlarmMessageRes.createRes(alarmMessage);
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "user-notification-routing-key", alarmMessageRes);
    }
}
