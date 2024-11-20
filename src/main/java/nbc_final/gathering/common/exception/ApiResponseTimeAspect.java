package nbc_final.gathering.common.exception;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ApiResponseTimeAspect {

    private static final Logger logger = LoggerFactory.getLogger(ApiResponseTimeAspect.class);

    @Pointcut("execution(public * nbc_final.gathering..*Controller.*(..))")  // 컨트롤러의 모든 메서드에 적용
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logExecutionTime(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();  // 실제 메서드 실행
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        logger.info("API: {} took {} ms", joinPoint.getSignature().toShortString(), duration);
        return proceed;
    }
}
