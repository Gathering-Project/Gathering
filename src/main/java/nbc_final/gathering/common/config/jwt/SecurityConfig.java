package nbc_final.gathering.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final nbc_final.gathering.common.config.JwtSecurityFilter jwtSecurityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // SessionManagementFilter, SecurityContextPersistenceFilter
                )
                .addFilterBefore(jwtSecurityFilter, SecurityContextHolderAwareRequestFilter.class)
                .formLogin(AbstractHttpConfigurer::disable) // UsernamePasswordAuthenticationFilter, DefaultLoginPageGeneratingFilter 비활성화
                .anonymous(AbstractHttpConfigurer::disable) // AnonymousAuthenticationFilter 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // BasicAuthenticationFilter 비활성화
                .logout(AbstractHttpConfigurer::disable) // LogoutFilter 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/users/login",
                                "/api/v1/users/signup",
                                "/login.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/api/v1/auth/kakao-url",
                                "/api/v1/users/kakao/callback",
                                "/api/v1/auth/naver-url",
                                "/api/v1/users/naver/callback",
                                "/chat/inbox"
//                                "/api/v1/gatherings/{gatheringId}",
//                                "/api/v1/gatherings"
                                ).permitAll()
//                        .requestMatchers("/test").hasAuthority(UserRole.Authority.ADMIN)
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
