package nbc_final.gathering.common.elasticsearch;

import nbc_final.gathering.domain.user.dto.UserElasticDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userElasticSearchRepo")
public interface UserElasticSearchRepository extends ElasticsearchRepository <UserElasticDto, Long> {

    List<UserElasticDto> findByNicknameContainingOrLocationContaining(String nickname, String location);
}
