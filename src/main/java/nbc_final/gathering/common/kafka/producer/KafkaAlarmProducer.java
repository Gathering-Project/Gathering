//package nbc_final.gathering.common.kafka.producer;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class KafkaAlarmProducer {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    public void notifyUser(Long userId, Object message) {
//        kafkaTemplate.send("user-notification-topic", userId.toString(), message);
//    }
//
//    public void notifyGuestMember(Long memberId, Object message) {
//        kafkaTemplate.send("guest-member-notification-topic", memberId.toString(), message);
//    }
//
//    public void notifyHostMember(Long memberId, Object message) {
//        kafkaTemplate.send("host-member-notification-topic", memberId.toString(), message);
//    }
//}