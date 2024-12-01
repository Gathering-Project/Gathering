package nbc_final.gathering.common.config;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class RedisMockConfig {

//    // Mock RedissonClient Bean
//    @Bean
//    public RedissonClient mockRedissonClient() {
//        return Mockito.mock(RedissonClient.class);
//    }

    @Bean
    @Primary // 테스트에서 이 RedisConnectionFactory를 우선적으로 사용
    public RedisConnectionFactory mockRedisConnectionFactory() {
        return Mockito.mock(RedisConnectionFactory.class);
    }

    @Bean
    @Primary // 테스트에서 이 RedisTemplate을 우선적으로 사용
    public RedisTemplate<String, Object> mockRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

}