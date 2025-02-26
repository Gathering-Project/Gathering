package nbc_final.gathering.common.config.jwt;

import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.user.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtSecurityFilter jwtSecurityFilter;
    @Value("${frontend.url}")
    private String frontendUrl;

    public SecurityConfig(JwtSecurityFilter jwtSecurityFilter) {
        this.jwtSecurityFilter = jwtSecurityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                                        "/payment-success",
                                        "/payment-fail",
                                        "/payment",
                                        "/api/v2/payments/ad",
                                        "/api/v1/users/login",
                                        "/api/v1/users/signup",
                                        "/api/v1/auth/**",
                                        "/api/v2/payments/success",
                                        "/api/v2/payments/fail",
                                        "/api/v1/auth/kakao-url",
                                        "/api/v1/users/kakao/callback",
                                        "/api/v1/auth/naver-url",
                                        "/api/v1/users/naver/callback",
                                        "/gathering/inbox/**",
                                        "/actuator/prometheus",
                                        "/actuator/health",
                                        "/api/v1/places/recommend",
                                        "/api/v1/users/naver/callback",
                                        "/gathering/inbox",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
//                                "/api/v1/gatherings/{gatheringId}",
//                                "/api/v1/gatherings"
                                ).permitAll()
                                .requestMatchers("/api/v1/members/gathering/1/request").hasAnyAuthority(UserRole.ROLE_USER.name())
                                .requestMatchers("/api/v1/members/1/approve").hasAuthority(MemberRole.HOST.name())
                                .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern(frontendUrl);
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
