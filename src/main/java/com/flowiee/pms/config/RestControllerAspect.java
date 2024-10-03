package com.flowiee.pms.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RestControllerAspect {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Before("execution(* com.flowiee.pms.controller.category.*.*(..))")
    public void logBeforeCategoryControllerCall(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("AOP Before call system controller {} with arguments: {}", joinPoint, Arrays.toString(args));
    }

    @Before("execution(* com.flowiee.pms.controller.product.*.*(..))")
    public void logBeforeProductControllerCall(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("AOP Before call system controller {} with arguments: {}", joinPoint, Arrays.toString(args));
    }

    @Before("execution(* com.flowiee.pms.controller.sales.*.*(..))")
    public void logBeforeSalesControllerCall(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("AOP Before call sales controller {} with arguments: {}", joinPoint, Arrays.toString(args));
    }

    @Before("execution(* com.flowiee.pms.controller.storage.*.*(..))")
    public void logBeforeStorageControllerCall(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("AOP Before call storage controller {} with arguments: {}", joinPoint, Arrays.toString(args));
    }

    @Before("execution(* com.flowiee.pms.controller.system.*.*(..))")
    public void logBeforeSystemControllerCall(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("AOP Before call system controller {} with arguments: {}", joinPoint, Arrays.toString(args));
    }
}