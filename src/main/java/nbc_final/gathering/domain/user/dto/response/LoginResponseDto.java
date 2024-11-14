package nbc_final.gathering.domain.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginResponseDto {

    private final String bearerToken;
    private final String websocketUrl;
}
