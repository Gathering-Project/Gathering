package nbc_final.gathering.domain.gathering.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.gathering.entity.Gathering;

@Getter
@AllArgsConstructor
public class GatheringResponseDto {

  private Long gatheringId;
  private String title;
  private String description;
  private String gatheringImage;
  private Integer gatheringMaxCount;
  private Integer gatheringCount;

  public static GatheringResponseDto of(Gathering gathering) {
    return new GatheringResponseDto(
        gathering.getId(),
        gathering.getTitle(),
        gathering.getDescription(),
        gathering.getGatheringImage(),
        gathering.getGatheringCount(),
        gathering.getGatheringMaxCount()
    );
  }





}




