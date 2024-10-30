package nbc_final.gathering.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import software.amazon.awssdk.services.s3.transform.GetObjectTorrentRequestMarshaller;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // JavaTimeModule 등록
        objectMapper.registerModule(new JavaTimeModule());

        // 직렬화 시 날짜를 타임스탬프가 아닌 ISO-8601 문자열로 출력하도록 설정 (선택 사항)
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // GenericJackson2JsonRedisSerializer에 ObjectMapper 추가
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);


        // Key는 String으로, Value는 JSON로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer); // JSON 직렬화

        return template;
    }


}
