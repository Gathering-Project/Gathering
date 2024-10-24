package nbc_final.gathering.domain.event.repository;

public interface EventRepositoryCustom {

    boolean isUserInGathering(Long gatheringId, Long userId);

    boolean isGatheringCreator(Long userId, Long gatheringId);

}
