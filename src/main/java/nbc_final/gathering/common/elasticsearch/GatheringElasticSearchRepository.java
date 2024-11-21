package nbc_final.gathering.common.elasticsearch;

import nbc_final.gathering.domain.gathering.dto.GatheringElasticDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("gatheringElasticSearchRepo")
public interface GatheringElasticSearchRepository extends ElasticsearchRepository<GatheringElasticDto, Long> {

    List<GatheringElasticDto> findByTitleContaining(String title);

    List<GatheringElasticDto> findByLocationContaining(String location);

    // 부분 검색
    List<GatheringElasticDto> findByTitleContainingAndLocationContaining(String title, String location);

}
