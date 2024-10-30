package nbc_final.gathering.domain.gathering.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GatheringRequestDto {

  private String title;
  private String description;
  private Integer gatheringMaxCount;
  private String location;

  @JsonCreator
  public GatheringRequestDto(
          @JsonProperty("title") String title,
          @JsonProperty("description") String description,
          @JsonProperty("gatheringMaxCount") Integer gatheringMaxCount,
          @JsonProperty("location") String location
  ) {
    this.title = title;
    this.description = description;
    this.gatheringMaxCount =gatheringMaxCount;
    this.location = location;
  }
}
