package nbc_final.gathering.common.elasticsearch;

import nbc_final.gathering.domain.gathering.entity.Gathering;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository("gatheringElasticSearchRepo")
public interface GatheringElasticSearchRepository extends ElasticsearchRepository <Gathering, Long> {
}