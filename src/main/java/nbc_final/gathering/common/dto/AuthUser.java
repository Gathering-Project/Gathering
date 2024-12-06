package nbc_final.gathering.common.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nbc_final.gathering.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AuthUser {

    private final Long userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String nickName;

    public AuthUser(Long userId, String email, UserRole role, String nickName) {
        this.userId = userId;
        this.email = email;
        this.authorities = List.of(new SimpleGrantedAuthority(role.name()));
        this.nickName = nickName;
    }

    public void setId(long l) {
        
    }
}
