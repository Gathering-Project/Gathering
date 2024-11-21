package nbc_final.gathering.common.alarmconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.alarmconfig.AlarmDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmMessageReceiver {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "${RABBITMQ_NOTIFICATION_QUEUE}")
    public void receiveAlarmMessage(byte[] message) {
        try {
            // byte[] 메시지를 AlarmMessageRes 객체로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            AlarmDto.AlarmMessageRes alarmMessageRes = objectMapper.readValue(message, AlarmDto.AlarmMessageRes.class);

            // RabbitMQ에서 메시지를 정상적으로 수신한 경우
            log.info("RabbitMQ에서 수신한 알람 메시지: {}", alarmMessageRes);

            // WebSocket으로 알람 메시지 전송 (엔드포인트 /gathering/inbox로 수정)
            messagingTemplate.convertAndSend("/gathering/inbox", alarmMessageRes.getMessage());
            log.info("WebSocket으로 알람 메시지 전송 완료: {}", alarmMessageRes.getMessage());
        } catch (Exception e) {
            log.error("메시지 변환 실패: {}", e.getMessage());
        }
    }

}
