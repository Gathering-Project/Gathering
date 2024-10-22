package nbc_final.gathering.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserChangePwRequestDto {

    private String oldPassword;
    private String newPassword;

}
