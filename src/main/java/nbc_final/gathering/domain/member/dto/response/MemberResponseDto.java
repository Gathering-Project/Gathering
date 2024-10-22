package nbc_final.gathering.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {
    private Long id;
    private Long gatheringId;
    private Long userId;
    private MemberRole role;
    private MemberStatus status;

    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.gatheringId = member.getGathering().getId();
        this.userId = member.getUser().getId();
        this.role = member.getRole();
        this.status = member.getStatus();
    }
}
