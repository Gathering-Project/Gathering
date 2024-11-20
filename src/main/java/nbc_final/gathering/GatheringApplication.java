package nbc_final.gathering;

import nbc_final.gathering.common.elasticsearch.EventElasticSearchRepository;
import nbc_final.gathering.common.elasticsearch.GatheringElasticSearchRepository;
import nbc_final.gathering.common.elasticsearch.MemberElasticSearchRepository;
import nbc_final.gathering.common.elasticsearch.UserElasticSearchRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = {
                "nbc_final.gathering.domain.attachment.repository",
                "nbc_final.gathering.domain.comment.repository",
                "nbc_final.gathering.domain.event.repository",
                "nbc_final.gathering.domain.gathering.repository",
                "nbc_final.gathering.domain.member.repository",
                "nbc_final.gathering.domain.user.repository",
                "nbc_final.gathering.domain.chatting.chatroom.repository",
                "nbc_final.gathering.domain.chatting.chatuser.repository",
                "nbc_final.gathering.domain.ad.repository",
                "nbc_final.gathering.domain.payment.repository"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        EventElasticSearchRepository.class,
                        GatheringElasticSearchRepository.class,
                        MemberElasticSearchRepository.class,
                        UserElasticSearchRepository.class
                }
        )
)
public class GatheringApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatheringApplication.class, args);
    }
}



