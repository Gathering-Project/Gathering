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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Document(indexName = "gatherings")
@NoArgsConstructor
public class GatheringElasticDto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL)
  private List<Member> members = new ArrayList<>();

  @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attachment> Attachments = new ArrayList<>();

  @Column(length = 30, nullable = false)
  private String title;

  @Column(length = 100, nullable = false)
  private String description;

  @Column(length = 2048)
  private String gatheringImage;

  @Column(nullable = false)
  private Integer gatheringCount;

  @Column(nullable = false)
  private Integer gatheringMaxCount;

  @Column(precision = 4, scale = 1, nullable = false)
  private BigDecimal rating;

  @Column(length = 30, nullable = false)
  private String location;

  private long totalGatheringViewCount;

  public GatheringElasticDto(Long userId,
                             String title,
                             String description,
                             Integer gatheringCount,
                             Integer gatheringMaxCount,
                             BigDecimal rating,
                             String location) {

    this.userId = userId;
    this.title = title;
    this.description = description;
    this.gatheringCount = gatheringCount;
    this.gatheringMaxCount = gatheringMaxCount;
    this.rating = rating;
    this.location = location;
  }


  public static GatheringElasticDto of(Gathering savedGathering) {
    GatheringElasticDto dto = new GatheringElasticDto();
    dto.setTitle(savedGathering.getTitle());
    dto.setDescription(savedGathering.getDescription());
    dto.setGatheringCount(savedGathering.getGatheringCount());
    dto.setGatheringMaxCount(savedGathering.getGatheringMaxCount());
    dto.setRating(savedGathering.getRating());
    dto.setLocation(savedGathering.getLocation());

    return dto;
  }
}
