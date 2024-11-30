package nbc_final.gathering.common.config;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RedisMockConfig {

    // Mock RedissonClient Bean
    @Bean
    public RedissonClient mockRedissonClient() {
        return Mockito.mock(RedissonClient.class);
    }

}