package nbc_final.gathering.domain.matching.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.common.kafka.util.KafkaNotificationUtil;
import nbc_final.gathering.domain.matching.dto.request.MatchingRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/matching")
public class MatchingController {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    /**
     * 매칭 대기열 참가 요청
     * @param requestDto
     * @return 매칭 서버에 데이터 전송 성공 여부
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> requestMatching(@RequestBody MatchingRequestDto requestDto) {
        kafkaTemplate.send("matching",requestDto);
        log.info("매칭 시작",requestDto);

        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 매칭 대기열 취소 요청
     * @param userId
     * @return 매칭 서버에 데이터 전송 성공 여부
     */
    @PostMapping("/cancel/{userId}")
    public ResponseEntity<ApiResponse<Void>> cancelMatching(@PathVariable Long userId) {
        kafkaTemplate.send("matching",userId);
        log.info("매칭 취소",userId);

        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

}
