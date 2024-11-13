package nbc_final.gathering.common.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.domain.matching.dto.request.MatchingRequestDto;
import nbc_final.gathering.domain.matching.dto.response.MatchingFailed;
import nbc_final.gathering.domain.matching.dto.response.MatchingResponseDto;
import nbc_final.gathering.domain.matching.dto.response.MatchingSuccess;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqConsumeService {

    @RabbitListener(queues = "matching.success")
    public void successMatchingConsumer(MatchingSuccess matchingSuccess) {
        log.info("유저 ID {}와 유저 ID {} 매칭 성공", matchingSuccess.getUserId1(), matchingSuccess.getUserId2());
    }

    @RabbitListener(queues = "matching.failed")
    public void failedMatchingConsumer(MatchingFailed matchingFailed) {
        log.info("유저 ID {} 매칭 실패", matchingFailed.getFailedUserId());
    }

}
