package nbc_final.gathering.domain.member.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;

@Getter
@NoArgsConstructor
public class MemberResponseDto {
    private Long id;
    private Long gatheringId;
    private Long userId;
    private MemberRole role;
    private MemberStatus status;

    private MemberResponseDto(Long id, Long gatheringId, Long userId, MemberRole role, MemberStatus status) {
        this.id = id;
        this.gatheringId = gatheringId;
        this.userId = userId;
        this.role = role;
        this.status = status;
    }

    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getGathering().getId(),
                member.getUser().getId(),
                member.getRole(),
                member.getStatus()
        );
    }
}