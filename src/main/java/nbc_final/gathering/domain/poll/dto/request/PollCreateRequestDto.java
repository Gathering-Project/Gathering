package nbc_final.gathering.domain.poll.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PollCreateRequestDto {

    @NotBlank(message = "투표 주제를 입력해주세요!")
    private String agenda;

    @Size(min = 2, message = "2개 이상의 선택지를 기입해주세요.")
    private List<String> options;

}
