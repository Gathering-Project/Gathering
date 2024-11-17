package nbc_final.gathering.common.elasticsearch.datagenerator;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.elasticsearch.GatheringElasticSearchRepository;
import nbc_final.gathering.domain.gathering.dto.GatheringElasticDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataGenerator {

    private final GatheringElasticSearchRepository gatheringElasticSearchRepository;

    /**
     * Elasticsearch에 데이터를 저장하는 로직
     *
     * @param numberOfEntries 생성할 데이터 수
     */
    public void generateAndSaveData(int numberOfEntries) {
        if (numberOfEntries > 100) {
            throw new IllegalArgumentException("최대 100개의 데이터만 생성할 수 있습니다.");
        }

        List<GatheringElasticDto> gatheringList = new ArrayList<>();
        Random random = new Random();

        // 데이터 생성
        for (int i = 1; i <= numberOfEntries; i++) {
            GatheringElasticDto gathering = new GatheringElasticDto();
            gathering.setId((long) i);
            gathering.setUserId((long) random.nextInt(100) + 1); // 랜덤 User ID (1 ~ 100)
            gathering.setTitle("Random Title " + random.nextInt(1000)); // 랜덤 제목
            gathering.setDescription("This is a random description for gathering " + i);
            gathering.setGatheringImage("https://example.com/image" + random.nextInt(100) + ".jpg");
            gathering.setGatheringCount(random.nextInt(50) + 1); // 현재 참여 인원 (1 ~ 50)
            gathering.setGatheringMaxCount(random.nextInt(50) + 50); // 최대 참여 인원 (50 ~ 100)
            gathering.setRating(BigDecimal.valueOf(1 + (5 - 1) * random.nextDouble())); // 평점 (1.0 ~ 5.0)
            gathering.setLocation("Location " + (random.nextInt(10) + 1)); // 랜덤 위치
            gathering.setTotalGatheringViewCount((long) random.nextInt(1000)); // 조회수 (0 ~ 1000)

            // 랜덤 멤버와 첨부 파일 ID 생성
            List<Long> memberIds = new ArrayList<>();
            List<Long> attachmentIds = new ArrayList<>();
            for (int j = 0; j < random.nextInt(5) + 1; j++) { // 1 ~ 5개의 멤버 ID
                memberIds.add((long) random.nextInt(1000));
            }
            for (int k = 0; k < random.nextInt(3) + 1; k++) { // 1 ~ 3개의 첨부파일 ID
                attachmentIds.add((long) random.nextInt(500));
            }
            gathering.setMemberIds(memberIds);
            gathering.setAttachmentIds(attachmentIds);

            gatheringList.add(gathering);
        }

        // Elasticsearch 저장
        try {
            gatheringElasticSearchRepository.saveAll(gatheringList);
            System.out.println("랜덤 데이터 저장 완료: " + numberOfEntries + "개의 데이터");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Elasticsearch 저장 중 오류 발생: " + e.getMessage());
        }
    }
}
