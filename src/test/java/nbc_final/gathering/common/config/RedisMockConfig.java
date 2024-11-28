package nbc_final.gathering.common.config;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RedisMockConfig {

    @MockBean
    private RedissonClient redissonClient;

    @Bean
    public RedissonClient mockRedissonClient() {
        return Mockito.mock(RedissonClient.class);
    }
}
