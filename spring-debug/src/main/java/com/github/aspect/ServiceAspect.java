package com.github.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author hangs.zhang
 * @date 2020/06/23 22:13
 * *****************
 * function:
 */
@Aspect
@Component
public class ServiceAspect {

    @Pointcut("execution(* com.github.service.impl.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        System.out.println("before:" + joinPoint.getSignature().getName());
    }

    @After("pointcut()")
    public void after(JoinPoint joinPoint) {
        System.out.println("after:" + joinPoint.getSignature().getName());
    }

}
