package nbc_final.gathering.domain.member.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "members")
public class MemberElasticDto {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long gatheringId;

    @Field(type = FieldType.Keyword)
    private MemberRole role;

    @Field(type = FieldType.Keyword)
    private MemberStatus status;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String roleDescription; // 역할 설명 필드 (예: "관리자", "참여자")

    public MemberElasticDto(Long userId, Long gatheringId, MemberRole role, String roleDescription) {
        this.userId = userId;
        this.gatheringId = gatheringId;
        this.role = role;
        this.roleDescription = roleDescription;
        this.status = MemberStatus.PENDING;
    }

    public MemberElasticDto(Long userId, Long gatheringId, MemberRole role, MemberStatus status, String roleDescription) {
        this.userId = userId;
        this.gatheringId = gatheringId;
        this.role = role;
        this.status = status;
        this.roleDescription = roleDescription;
    }

    public static MemberElasticDto of(Member member) {
        return new MemberElasticDto(
                member.getId(),
                member.getGathering().getUserId(),
                member.getGathering().getId(),
                member.getRole(),
                member.getStatus(),
                member.getRole().name() // roleDescription에 역할 이름 추가
        );
    }
}

