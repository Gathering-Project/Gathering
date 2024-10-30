package nbc_final.gathering.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoDto {
    private String id;
    private String nickname;
    private String email;

    public static NaverUserInfoDto of(String id, String nickname, String email) {
        NaverUserInfoDto dto = new NaverUserInfoDto();
        dto.id = id;
        dto.nickname = nickname;
        dto.email = email;
        return dto;
    }
}
