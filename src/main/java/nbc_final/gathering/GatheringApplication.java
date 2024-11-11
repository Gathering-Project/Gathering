package nbc_final.gathering;

import jakarta.persistence.Entity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableElasticsearchRepositories(basePackages = "nbc_final.gathering.common.elasticsearch")
@EnableJpaRepositories(basePackages = {"nbc_final.gathering.domain.attachment.repository","nbc_final.gathering.domain.comment.repository","nbc_final.gathering.domain.event.repository",
"nbc_final.gathering.domain.gathering.repository","nbc_final.gathering.domain.member.repository","nbc_final.gathering.domain.user.repository"}
        )
public class GatheringApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatheringApplication.class, args);
    }

}
