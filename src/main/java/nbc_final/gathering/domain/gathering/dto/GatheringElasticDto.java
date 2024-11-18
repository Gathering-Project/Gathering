package nbc_final.gathering.domain.gathering.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.user.entity.User;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@Document(indexName = "gatherings")
@NoArgsConstructor
@AllArgsConstructor
public class GatheringElasticDto {
  @Id
  private Long id;

  private Long userId;

  @Field(type = FieldType.Keyword)
  private List<Long> memberIds = new ArrayList<>();

  @Field(type = FieldType.Keyword)
  private List<Long> attachmentIds = new ArrayList<>();

  @Field(type = FieldType.Text, analyzer = "nori_analyzer")
  private String title;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String description;

  @Field(type = FieldType.Text)
  private String gatheringImage;

  @Field(type = FieldType.Integer)
  private Integer gatheringCount;

  @Field(type = FieldType.Integer)
  private Integer gatheringMaxCount;

  @Field(type = FieldType.Double)
  private BigDecimal rating;

  @Field(type = FieldType.Text, analyzer = "nori_analyzer")
  private String location;

  @Field(type = FieldType.Long)
  private long totalGatheringViewCount;

  public static GatheringElasticDto of(Gathering savedGathering) {
    GatheringElasticDto dto = new GatheringElasticDto();
    dto.setId(savedGathering.getId());
    dto.setUserId(savedGathering.getUserId());
    dto.setTitle(savedGathering.getTitle());
    dto.setDescription(savedGathering.getDescription());
    dto.setGatheringImage(savedGathering.getGatheringImage());
    dto.setGatheringCount(savedGathering.getGatheringCount());
    dto.setGatheringMaxCount(savedGathering.getGatheringMaxCount());
    dto.setRating(savedGathering.getRating());
    dto.setLocation(savedGathering.getLocation());
    dto.setTotalGatheringViewCount(savedGathering.getTotalGatheringViewCount());

    // Member 및 Attachment ID 매핑
    dto.setMemberIds(savedGathering.getMembers().stream()
            .map(member -> member.getId())
            .collect(Collectors.toList()));
    dto.setAttachmentIds(savedGathering.getAttachments().stream()
            .map(attachment -> attachment.getId())
            .collect(Collectors.toList()));

    return dto;
  }
}
