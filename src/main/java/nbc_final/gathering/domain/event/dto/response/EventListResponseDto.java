package nbc_final.gathering.domain.event.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.event.entity.Event;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class EventListResponseDto {

    private List<EventResponseDto> events;

    @JsonCreator  // Jackson에서 역직렬화 가능하도록 생성자 추가
    public EventListResponseDto(
            @JsonProperty("events"
            ) List<EventResponseDto> events) {
        this.events = events;
    }

    public static EventListResponseDto of(List<Event> eventList, Long userId) {
        List<EventResponseDto> eventResponseDtos = eventList.stream()
                .map(event -> EventResponseDto.of(event, userId))
                .collect(Collectors.toList());
        return new EventListResponseDto(eventResponseDtos);
    }
}
