package nbc_final.gathering.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommandResponseDto {
    private List<PlaceDto> places;
}
