package nbc_final.gathering.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoDto {
    private Long id;
    private String nickname;
    private String email;

    public static KakaoUserInfoDto of(Long id, String nickname, String email) {
        KakaoUserInfoDto dto = new KakaoUserInfoDto();
        dto.id = id;
        dto.nickname = nickname;
        dto.email = email;
        return dto;
    }
}
