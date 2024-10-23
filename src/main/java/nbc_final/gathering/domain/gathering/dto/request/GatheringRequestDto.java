package nbc_final.gathering.domain.gathering.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GatheringRequestDto {

  private String title;
  private String description;
  private String gatheringImage;
  private Integer gatheringMaxCount;
  private String location;
}
