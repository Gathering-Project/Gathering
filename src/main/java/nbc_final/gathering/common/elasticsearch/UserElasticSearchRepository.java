package nbc_final.gathering.common.elasticsearch;

import nbc_final.gathering.domain.user.dto.UserElasticDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository("UserElasticSearchRepo")
public interface UserElasticSearchRepository extends ElasticsearchRepository <UserElasticDto, Long> {
}
