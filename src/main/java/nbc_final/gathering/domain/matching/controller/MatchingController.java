package nbc_final.gathering.domain.matching.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.matching.dto.request.MatchingRequestDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/matching")
public class MatchingController {

//    private final KafkaTemplate<String, Object> kafkaTemplate; // 카프카 사용 version
    private final RabbitTemplate rabbitTemplate; // RabbitMQ 사용 version

    /**
     * 매칭 대기열 참가 요청
     * @param requestDto
     * @return 매칭 서버에 데이터 전송 성공 여부
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> requestMatching(@RequestBody MatchingRequestDto requestDto) {
//        kafkaTemplate.send("matching",requestDto);
        rabbitTemplate.convertAndSend("matching.exchange", "matching.request", requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 매칭 대기열 취소 요청
     * @param userId
     * @return 매칭 서버에 데이터 전송 성공 여부
     */
    @PostMapping("/cancel/{userId}")
    public ResponseEntity<ApiResponse<Void>> cancelMatching(@PathVariable Long userId) {
//        kafkaTemplate.send("matching",userId);
        rabbitTemplate.convertAndSend("matching.exchange", "matching.cancel", userId);
        log.info("매칭 취소",userId);

        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

}
