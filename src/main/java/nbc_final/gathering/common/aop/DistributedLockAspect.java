package nbc_final.gathering.common.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.annotation.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    /**
     * 분산 락 처리 AOP
     *
     * @param joinPoint       메서드 실행 지점
     * @param distributedLock 분산 락 어노테이션 정보
     * @return 메서드 실행 결과
     */
    @Around("@annotation(distributedLock)")
    public Object handleDistributedLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = distributedLock.key();
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();

        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도
            if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                log.info("Lock acquired: {}", lockKey);
                return joinPoint.proceed(); // 락 획득 후 메서드 실행
            } else {
                log.warn("Failed to acquire lock: {}", lockKey);
                throw new IllegalStateException("Could not acquire lock: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted: {}", lockKey, e);
            throw new IllegalStateException("Lock acquisition interrupted: " + lockKey, e);
        } finally {
            // 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released: {}", lockKey);
            }
        }
    }
}
