package nbc_final.gathering.domain.gathering.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.member.entity.Member;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gatherings")
public class Gathering extends TimeStamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Member> members = new ArrayList<>();

  @Column(length = 30, nullable = false)
  private String title;

  @Column(length = 100, nullable = false)
  private String description;

  @Column(length = 2048, nullable = false)
  private String gatheringImage;

  @Column(nullable = false)
  private Integer gatheringCount;

  @Column(nullable = false)
  private Integer gatheringMaxCount;

  @Column(precision = 4, scale = 1, nullable = false)
  private BigDecimal rating;

  @Column(length = 30, nullable = false)
  private String location;

  public Gathering(String title,
                   String description,
                   String gatheringImage,
                   Integer gatheringCount,
                   Integer gatheringMaxCount,
                   BigDecimal rating,
                   String location) {

    this.title = title;
    this.description = description;
    this.gatheringImage = gatheringImage;
    this.gatheringCount = gatheringCount;
    this.gatheringMaxCount = gatheringMaxCount;
    this.rating = rating;
    this.location = location;
  }

  public void setGatheringImage(String gatheringImage) {
    this.gatheringImage = gatheringImage;
  }

  public void updateDetails(String title,
                            String description,
                            Integer gatheringMaxCount,
                            String location,
                            String gatheringImage) {
    this.title = title;
    this.description = description;
    this.gatheringMaxCount = gatheringMaxCount;
    this.location = location;
    this.gatheringImage = gatheringImage;
  }
}