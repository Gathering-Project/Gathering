package nbc_final.gathering.domain.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SignUpResponseDto {

    private final String bearerToken;

}
