package nbc_final.gathering.domain.gathering.dto.response;

import lombok.Getter;
import nbc_final.gathering.domain.gathering.entity.Gathering;

@Getter
public class GatheringResponseDto {

  private Long gatheringId;
  private String title;
  private String description;
  private String gatheringImage;
  private Integer gatheringMaxCount;
  private Integer gatheringCount;

  public GatheringResponseDto(Gathering gathering) {
    this.gatheringId = gathering.getId();
    this.title = gathering.getTitle();
    this.description = gathering.getDescription();
    this.gatheringImage = gathering.getGatheringImage();
    this.gatheringCount = gathering.getGatheringCount();
    this.gatheringMaxCount = gathering.getGatheringMaxCount();
  }
}




