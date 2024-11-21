package nbc_final.gathering.domain.gathering.dto.response;

import lombok.Getter;
import nbc_final.gathering.domain.gathering.entity.Gathering;

@Getter
public class GatheringWithCountResponseDto {
    private Long gatheringId;
    private String title;
    private String description;
    private String gatheringImage;
    private Integer gatheringMaxCount;
    private Integer gatheringCount;
    private Long todayGatheringViewCount;
    private Long totalGatheringViewCount;

    public GatheringWithCountResponseDto(Gathering gathering, Long todayGatheringViewCount) {
        this.gatheringId = gathering.getId();
        this.title = gathering.getTitle();
        this.description = gathering.getDescription();
        this.gatheringImage = gathering.getGatheringImage();
        this.gatheringMaxCount = gathering.getGatheringMaxCount();
        this.gatheringCount = gathering.getGatheringCount();
        this.todayGatheringViewCount = todayGatheringViewCount;
        this.totalGatheringViewCount = gathering.getTotalGatheringViewCount();
    }
}
