package nbc_final.gathering.common.elasticsearch;

import nbc_final.gathering.domain.member.dto.MemberElasticDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("memberElasticSearchRepo")
public interface MemberElasticSearchRepository extends ElasticsearchRepository<MemberElasticDto, Long> {

    List<MemberElasticDto> findByGatheringId(Long gatheringId);

}
