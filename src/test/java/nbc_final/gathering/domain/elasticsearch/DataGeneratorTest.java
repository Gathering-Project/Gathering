//package nbc_final.gathering.domain.elasticsearch;
//
//import nbc_final.gathering.common.elasticsearch.GatheringElasticSearchRepository;
//import nbc_final.gathering.domain.gathering.dto.GatheringElasticDto;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//public class DataGeneratorTest {
//
//    @Autowired
//    DataGeneratorTest dataGenerator;
//
//    @Autowired
//    GatheringElasticSearchRepository gatheringElasticSearchRepository;
//
//    @Test
//    void test_dataGenerator() {
//        int testCount = 100;
//
//        dataGenerator.generateAndSaveData(testCount);
//
//        for (GatheringElasticDto gatheringElasticDto : gatheringElasticSearchRepository.findAll()) {
//            System.out.println("gatheringElasticDto = " + gatheringElasticDto);
//        }
//
//    }
//
//}
