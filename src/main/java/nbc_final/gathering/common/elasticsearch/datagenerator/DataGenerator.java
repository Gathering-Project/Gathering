package nbc_final.gathering.common.elasticsearch.datagenerator;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.elasticsearch.EventElasticSearchRepository;
import nbc_final.gathering.domain.event.dto.EventElasticDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DataGenerator {

    private final EventElasticSearchRepository eventElasticSearchRepository;

    /**
     * Elasticsearch에 데이터를 저장하는 로직
     *
     * @param numberOfEntries 생성할 데이터 수
     */
    public void generateAndSaveData(int numberOfEntries) {
        if (numberOfEntries > 100) {
            throw new IllegalArgumentException("최대 100개의 데이터만 생성할 수 있습니다.");
        }

        List<EventElasticDto> eventElasticDtos = new ArrayList<>();
        Random random = new Random();

        // 데이터 생성
        for (int i = 1; i <= numberOfEntries; i++) {
            EventElasticDto eventElasticDto = new EventElasticDto(
                    "Random Title " + random.nextInt(1000), // 제목
                    "This is a random description for event " + i, // 설명
                    "2024-11-" + (random.nextInt(30) + 1), // 랜덤 날짜
                    "Location " + (random.nextInt(10) + 1), // 랜덤 위치
                    random.nextInt(50) + 50, // 최대 참여 인원 (50 ~ 100)
                    (long) random.nextInt(100) + 1, // 랜덤 Gathering ID
                    (long) random.nextInt(100) + 1, // 랜덤 User ID
                    generateRandomIds(random, 5, 1000) // 랜덤 참가자 IDs
            );

            // 추가 설정 (필요시 추가 필드 초기화)
            eventElasticDtos.add(eventElasticDto);
        }

        // Elasticsearch 저장
        try {
            eventElasticSearchRepository.saveAll(eventElasticDtos);
            System.out.println("랜덤 데이터 저장 완료: " + numberOfEntries + "개의 데이터");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Elasticsearch 저장 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 랜덤 ID 리스트 생성
     *
     * @param random       랜덤 객체
     * @param maxCount     생성할 최대 개수
     * @param idRangeLimit ID 값 범위 제한
     * @return 랜덤 ID 리스트
     */
    private List<Long> generateRandomIds(Random random, int maxCount, int idRangeLimit) {
        List<Long> ids = new ArrayList<>();
        int count = random.nextInt(maxCount) + 1; // 1 ~ maxCount 개수 생성
        for (int i = 0; i < count; i++) {
            ids.add((long) random.nextInt(idRangeLimit));
        }
        return ids;
    }
}
