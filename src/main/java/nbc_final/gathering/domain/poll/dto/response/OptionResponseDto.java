package nbc_final.gathering.domain.poll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptionResponseDto {

    private Long id;
    private String name;
    private int voteCount;

}
