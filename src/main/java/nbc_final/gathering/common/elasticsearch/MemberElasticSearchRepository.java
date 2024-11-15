package nbc_final.gathering.common.elasticsearch;

import nbc_final.gathering.domain.member.dto.MemberElasticDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository("memberElasticSearchRepo")
public interface MemberElasticSearchRepository extends ElasticsearchRepository <MemberElasticDto, Long> {

}
