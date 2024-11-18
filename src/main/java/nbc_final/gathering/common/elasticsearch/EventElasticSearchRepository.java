package nbc_final.gathering.common.elasticsearch;

import nbc_final.gathering.domain.event.dto.EventElasticDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("eventElasticSearchRepo")
public interface EventElasticSearchRepository extends ElasticsearchRepository <EventElasticDto, Long> {
    List<EventElasticDto> findByTitleContainingOrDescriptionContaining(String title, String description);
    List<EventElasticDto> findByTitleContaining(String keyword);
    List<EventElasticDto> findByDescriptionContaining(String keyword);
    List<EventElasticDto> findByLocationContaining(String keyword);

}
