package nbc_final.gathering.domain.event.dto;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.event.entity.Participant;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "events")
public class EventElasticDto {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String date;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String location;

    @Field(type = FieldType.Integer)
    private Integer maxParticipants;

    @Field(type = FieldType.Integer)
    private Integer currentParticipants = 0;

    @Field(type = FieldType.Long)
    private Long gatheringId;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private List<Long> participantIds;


    public EventElasticDto(String title, String description, String date, String location, Integer maxParticipants, Long gatheringId, Long userId, List<Long> participantIds) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.gatheringId = gatheringId;
        this.userId = userId;
        this.participantIds = participantIds;
    }

    public static EventElasticDto of(Event event) {
        return new EventElasticDto(
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getMaxParticipants(),
                event.getId(),
                event.getUser().getId(),
                event.getParticipants().stream()
                        .map(Participant::getId)
                        .collect(Collectors.toList())
        );
    }
}
