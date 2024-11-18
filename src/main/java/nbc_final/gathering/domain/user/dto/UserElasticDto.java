package nbc_final.gathering.domain.user.dto;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.user.enums.MbtiType;
import nbc_final.gathering.domain.user.enums.UserRole;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Document(indexName = "users")
public class UserElasticDto {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long kakaoId;

    @Field(type = FieldType.Keyword)
    private String naverId;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer") // nori_analyzer 적용
    private String location;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer") // nori_analyzer 적용
    private String nickname;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Boolean)
    private boolean isDeleted = false;

    @Field(type = FieldType.Keyword)
    private InterestType interestType;

    @Field(type = FieldType.Keyword)
    private MbtiType mbtiType;

    @Field(type = FieldType.Date)
    private LocalDateTime withdrawalDate;

    @Field(type = FieldType.Keyword)
    private UserRole userRole;

    @Field(type = FieldType.Text)
    private String profileImagePath;

    @Builder
    public UserElasticDto(Long id, Long kakaoId, String naverId, String location, String nickname,
                          String email, boolean isDeleted, InterestType interestType, MbtiType mbtiType,
                          LocalDateTime withdrawalDate, UserRole userRole, String profileImagePath) {
        this.id = id;
        this.kakaoId = kakaoId;
        this.naverId = naverId;
        this.location = location;
        this.nickname = nickname;
        this.email = email;
        this.isDeleted = isDeleted;
        this.interestType = interestType;
    }

    public static UserElasticDto of(User savedUser) {
        return UserElasticDto.builder()
                .id(savedUser.getId())
                .kakaoId(savedUser.getKakaoId())
                .naverId(savedUser.getNaverId())
                .location(savedUser.getLocation())
                .nickname(savedUser.getNickname())
                .email(savedUser.getEmail())
                .isDeleted(savedUser.isDeleted())
                .interestType(savedUser.getInterestType())
                .mbtiType(savedUser.getMbtiType())
                .withdrawalDate(savedUser.getWithdrawalDate())
                .userRole(savedUser.getUserRole())
                .profileImagePath(savedUser.getProfileImagePath())
                .build();
    }
}

