package nbc_final.gathering.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.member.enums.MemberRole;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequestDto {
    private Long gatheringId;
    private Long userId;
    private MemberRole role;
}