package nbc_final.gathering.domain.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.matching.dto.request.MatchingRequestDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matching")
@Tag(name = "Matching API", description = "1:1 유저 매칭 관련 API 모음입니다.")
public class MatchingController {

    //    private final KafkaTemplate<String, Object> kafkaTemplate; // 카프카 사용 시
    private final RabbitTemplate rabbitTemplate;

    /**
     * 매칭 대기열 참가 요청
     *
     * @param requestDto
     * @return 매칭 서버에 데이터 전송 성공 여부
     */
    @PostMapping
    @Operation(summary = "1:1 매칭 신청", description = "지역, 관심사(취미)가 동일한 다른 유저와의 1:1 매칭을 요청합니다.")
    public ResponseEntity<ApiResponse<Void>> requestMatching(@RequestBody MatchingRequestDto requestDto) {
        log.info("유저 ID {} 매칭 요청", requestDto.getUserId());
//        kafkaTemplate.send("matching",requestDto);
        rabbitTemplate.convertAndSend("matching.exchange", "matching.request", requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }


    /**
     * 매칭 대기열 취소 요청
     *
     * @param userId
     * @return 매칭 서버에 데이터 전송 성공 여부
     */
    @Operation(summary = "매칭 취소", description = "매칭을 취소하고 매칭 대기열에서 빠져나옵니다.")
    @PostMapping("/cancel/{userId}")
    public ResponseEntity<ApiResponse<Void>> cancelMatching(@PathVariable Long userId) {
//        kafkaTemplate.send("matching",userId);
        rabbitTemplate.convertAndSend("matching.exchange", "matching.cancel", userId);
        log.info("매칭 취소", userId);

        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

}
