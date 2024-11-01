package nbc_final.gathering.domain.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.user.entity.User;

@Getter
@NoArgsConstructor
public class EventCreateRequestDto {

    @NotBlank(message = "이벤트 제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "이벤트 설명을 입력해주세요.")
    private String description;

    @NotNull(message = "이벤트 날짜를 입력해주세요.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜는 'YYYY-MM-DD' 형식이어야 합니다.")
    private String date;

    @NotBlank(message = "이벤트 장소를 입력해주세요.")
    private String location;

    @NotNull(message = "최대 참가자 수를 입력해주세요.")
    private Integer maxParticipants;

    private EventCreateRequestDto(String title, String description, String date, String location, Integer maxParticipants) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.maxParticipants = maxParticipants;
    }

    public static EventCreateRequestDto of(String title, String description, String date, String location, Integer maxParticipants) {
        return new EventCreateRequestDto(title, description, date, location, maxParticipants);
    }

}
