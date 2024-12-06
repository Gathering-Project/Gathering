package nbc_final.gathering.common.config.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration    // 스프링 실행시 설정파일 읽어드리기 위한 어노테이션
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    // Swagger에서 보여줄 API 정보를 설정
    private Info apiInfo() {
        return new Info()
                .title("한달인턴 7기 과제: Swagger")
                .description("유저 회원가입, 로그인 REST API")
                .version("1.0.0");
    }
}

