package com.fintory.auth.util.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class AuthServicePerformanceLoggingAspect {

    @Around("execution(* com.fintory.auth.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        Object proceed = joinPoint.proceed();

        stopWatch.stop();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        long executionTime = stopWatch.getTotalTimeMillis();

        // 10ms 이상 걸린 메서드만 로그로 출력하여 로그 폭발을 방지
        if (executionTime > 10) {
            log.info("[PERF] {}.{} execution time: {}ms",
                    className, methodName, executionTime);
        }

        return proceed;
    }

}
