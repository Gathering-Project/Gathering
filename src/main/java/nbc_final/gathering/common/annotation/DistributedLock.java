package nbc_final.gathering.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key(); // Lock의 이름 (고유값)

    long waitTime() default 10;  // Lock획득을 시도하는 최대 시간 (ms)

    long leaseTime() default 15; // 락을 획득한 후, 점유하는 최대 시간 (ms)
}
