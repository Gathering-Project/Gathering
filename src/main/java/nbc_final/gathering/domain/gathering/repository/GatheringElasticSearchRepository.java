package nbc_final.gathering.domain.gathering.repository;

import nbc_final.gathering.domain.gathering.entity.Gathering;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GatheringElasticSearchRepository extends ElasticsearchRepository <Gathering, Long> {
}