package nbc_final.gathering.common.config.chatconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@RequiredArgsConstructor
@EnableMongoRepositories("nbc_final.gathering.domain.chatting.chatmessage.repository")
@EnableMongoAuditing
public class MongoDBConfig {

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory mongoDatabaseFactory, MongoMappingContext mongoMappingContext) {
        // DbRefResolver를 생성하여 MongoDB의 DBRef 필드 해석을 처리
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);

        // MongoDB에서 사용할 MappingMongoConverter를 생성
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);

        // MongoDB document에 기본적으로 추가되는 '_class' 필드를 제거
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return converter; // 커스텀 설정이 적용된 MappingMongoConverter 반환
    }

}
