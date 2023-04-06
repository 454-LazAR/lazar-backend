package com.lazar.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

/**
 *  Uncomment for more exhaustive logging. Seemed unnecessary for our purposes.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(name = "logging.enabled", havingValue = "true")
public class LoggingAspect {

//    @Pointcut("execution(* com.lazar.core.*.*(..)) || execution(* com.lazar.controller.GameController.*(..)) || execution(* com.lazar.persistence.*.*(..))")
    @Pointcut("execution(* com.lazar.controller.GameController.*(..))")
    public void logMethod() {}

    @Before("logMethod()")
    public void logBeforeMethodCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info("CONTROLLER - Method {} is called with arguments {}.", methodName, Arrays.toString(joinPoint.getArgs()));

//        String packageName = joinPoint.getTarget().getClass().getPackage().getName();
//        if(joinPoint.getTarget().getClass().getPackage().getName().startsWith("com.lazar.controller")){
//            log.info("CONTROLLER - Method {} is called with arguments {}.", methodName, Arrays.toString(joinPoint.getArgs()));
//        } else if (packageName.startsWith("com.lazar.core")) {
//            log.info("SERVICE - Method {} is called with arguments {}.", methodName, Arrays.toString(joinPoint.getArgs()));
//        } else {
//            log.info("PERSISTENCE - Method {} is called with arguments {}.", methodName, Arrays.toString(joinPoint.getArgs()));
//        }
    }

    @AfterReturning(pointcut = "logMethod()", returning = "returnValue")
    public void logAfterMethodReturn(JoinPoint joinPoint, Object returnValue) {
        String methodName = joinPoint.getSignature().getName();
        log.info("CONTROLLER - Method {} returns with value {}.", methodName, returnValue);

//        String packageName = joinPoint.getTarget().getClass().getPackage().getName();
//        if(packageName.startsWith("com.lazar.controller")){
//            log.info("CONTROLLER - Method {} returns with value {}.", methodName, returnValue);
//        } else if (packageName.startsWith("com.lazar.core")){
//            log.info("CORE - Method {} returns with value {}.", methodName, returnValue);
//        } else {
//            log.info("PERSISTENCE - Method {} returns with value {}.", methodName, returnValue);
//        }
    }

    @AfterThrowing(pointcut = "logMethod()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, ResponseStatusException ex) {
        String methodName = joinPoint.getSignature().getName();
        if(ex.getStatusCode().value() / 100 == 4) {
            log.warn("CONTROLLER - Method {} threw an exception: {}", methodName, ex.getMessage());
        } else {
            log.error("CONTROLLER - Method {} threw an exception: {}", methodName, ex.getMessage());
        }

//        String packageName = joinPoint.getTarget().getClass().getPackage().getName();
//        if(packageName.startsWith("com.lazar.controller")){
//            log.error("CONTROLLER - Method {} threw an exception: {}", methodName, ex.getMessage());
//        } else if (packageName.startsWith("com.lazar.core")){
//            log.error("CORE - Method {} threw an exception: {}", methodName, ex.getMessage());
//        } else {
//            log.error("PERSISTENCE - Method {} threw an exception: {}", methodName, ex.getMessage());
//        }
    }
}
