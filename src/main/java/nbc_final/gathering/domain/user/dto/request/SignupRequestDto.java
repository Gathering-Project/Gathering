package nbc_final.gathering.domain.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.user.enums.MbtiType;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank
    private String location; // 사용자 거주 지역
    private String nickname;

    @Email @NotBlank
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\p{Punct}])[A-Za-z\\d\\p{Punct}]{8,20}$",
            message = "비밀번호는 8자 이상 20자 이하이어야 하며, 적어도 하나의 알파벳, 하나의 숫자, 하나의 특수 문자를 포함해야 합니다."
    )
    private String password;

    @NotNull
//    private List<InterestType> interestTypes;
    private InterestType interestType;

    @NotNull
    private MbtiType mbtiType;

    @NotBlank
    private String userRole;



//    private boolean admin = false;
//    private String adminToken ="";

}
