package nbc_final.gathering.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePwRequestDto {

    private String oldPassword;
    private String newPassword;

}
