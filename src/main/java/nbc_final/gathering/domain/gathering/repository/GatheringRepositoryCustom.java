package nbc_final.gathering.domain.gathering.repository;

import nbc_final.gathering.domain.gathering.entity.Gathering;

import java.util.List;

public interface GatheringRepositoryCustom {
    List<Gathering> searchGatheringsByTitle(String title);
    List<Gathering> searchGatheringsByLocation(String location);
    List<Gathering> searchGatheringsByTitleAndLocation(String title, String location);
}
