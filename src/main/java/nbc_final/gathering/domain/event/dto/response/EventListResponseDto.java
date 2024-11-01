package nbc_final.gathering.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.event.entity.Event;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class EventListResponseDto {

    private List<EventResponseDto> events;

    public static EventListResponseDto of(List<Event> eventList, Long userId) {
        List<EventResponseDto> eventResponseDtos = eventList.stream()
                .map(event -> EventResponseDto.of(event, userId))
                .collect(Collectors.toList());
        return new EventListResponseDto(eventResponseDtos);
    }

    public static EventListResponseDto of(List<EventResponseDto> eventResponseDtos) {
        return new EventListResponseDto(eventResponseDtos);
    }
}
