package nbc_final.gathering.domain.gathering.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class GatheringTopViewListResponseDto {
    private final List<String> topViewGatheringList;
}