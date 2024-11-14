package nbc_final.gathering.common.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Autowired
    private RedisSentinelProperties redisSentinelProperties;

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime을 지원하도록 모듈 등록
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 직렬화 시 타임스탬프 사용하지 않음
        return objectMapper;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis Sentinel 설정
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();

        final RedisSentinelProperties.RedisMasterProperties masterConfig = redisSentinelProperties.getMaster();
        RedisStaticMasterReplicaConfiguration staticMasterReplicaConfiguration =
                new RedisStaticMasterReplicaConfiguration(masterConfig.getHost(), masterConfig.getPort());

        redisSentinelProperties.getNodes().forEach(node -> {
            String[] nodeInfo = node.split(":");
            staticMasterReplicaConfiguration.addNode(nodeInfo[0], Integer.parseInt(nodeInfo[1]));
        });

        return new LettuceConnectionFactory(staticMasterReplicaConfiguration, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // GenericJackson2JsonRedisSerializer에 ObjectMapper를 주입하여 직렬화 설정
        GenericJackson2JsonRedisSerializer genericSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Key는 String으로, Value는 JSON으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(genericSerializer); // Value에 JSON 직렬화 사용
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(genericSerializer); // Hash Value에도 JSON 직렬화 사용

        return template;
    }
}
