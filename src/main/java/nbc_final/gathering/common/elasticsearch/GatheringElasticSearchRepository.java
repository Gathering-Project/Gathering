package nbc_final.gathering.common.elasticsearch;

import nbc_final.gathering.domain.gathering.dto.GatheringElasticDto;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("gatheringElasticSearchRepo")
public interface GatheringElasticSearchRepository extends ElasticsearchRepository <GatheringElasticDto, Long> {

    List<Gathering> findByTitleAndLocation(String title, String location);
}