package nbc_final.gathering.domain.gathering.dto.response;

import lombok.Getter;
import nbc_final.gathering.domain.gathering.entity.Gathering;

@Getter
public class GatheringResponseDto {

  private String title;
  private String description;
  private Integer gatheringMaxCount;
  private Integer gatheringCount;

  public GatheringResponseDto(Gathering gathering) {
    this.title = gathering.getTitle();
    this.description = gathering.getDescription();
    this.gatheringCount = gathering.getGatheringCount();
    this.gatheringMaxCount = gathering.getGatheringMaxCount();
  }
}




