package nbc_final.gathering.domain.gathering.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonCreator
  public GatheringResponseDto(
          @JsonProperty("gatheringId") Long gatheringId,
          @JsonProperty("title") String title,
          @JsonProperty("description") String description,
          @JsonProperty("gatheringImage") String gatheringImage,
          @JsonProperty("gatheringMaxCount") Integer gatheringMaxCount,
          @JsonProperty("gatheringCount") Integer gatheringCount
  ) {
    this.gatheringId = gatheringId;
    this.title = title;
    this.description = description;
    this.gatheringImage = gatheringImage;
    this.gatheringMaxCount = gatheringMaxCount;
    this.gatheringCount = gatheringCount;
  }

  public static GatheringResponseDto of(Gathering gathering) {
    return new GatheringResponseDto(
        gathering.getId(),
        gathering.getTitle(),
        gathering.getDescription(),
        gathering.getGatheringImage(),
        gathering.getGatheringMaxCount(),
        gathering.getGatheringCount()
    );
  }





}




