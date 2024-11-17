package nbc_final.gathering.domain.gathering.repository;

import nbc_final.gathering.common.elasticsearch.GatheringElasticSearchRepository;
import nbc_final.gathering.domain.gathering.dto.GatheringElasticDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GatheringElasticSearchRepositoryTest {

    @Autowired
    private GatheringElasticSearchRepository gatheringElasticSearchRepository;

    @Test
    public void testSaveToElasticsearch() {
        GatheringElasticDto gathering = new GatheringElasticDto();
        gathering.setId(1L);
        gathering.setUserId(1L);
        gathering.setTitle("Test Title");
        gathering.setDescription("Test Description");
        gathering.setGatheringCount(10);
        gathering.setGatheringMaxCount(50);
        gathering.setRating(BigDecimal.valueOf(4.5));
        gathering.setLocation("Test Location");

        gatheringElasticSearchRepository.save(gathering);

        System.out.println("데이터 저장 완료");
    }
}
