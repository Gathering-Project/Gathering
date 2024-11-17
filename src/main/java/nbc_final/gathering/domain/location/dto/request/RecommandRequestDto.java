package nbc_final.gathering.domain.location.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommandRequestDto {
  private String address;
  private String type;
  private int radius;
}
