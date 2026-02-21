package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    // 切点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){};


    /**
     * 前置通知，在方法执行前进行数据填充
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行数据填充");

        //获取当前被拦截的数据库操作类型
        MethodSignature  signature= (MethodSignature) joinPoint.getSignature();//方法签名对象
        OperationType operationType = signature.getMethod().getAnnotation(AutoFill.class).value();//获取操作类型

        //获取被拦截的方法参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            return;
        }
        Object entity =  args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据操作类型，为对应的属性赋值(反射)
        if (operationType == OperationType.INSERT){
            //为四个字段赋值
            try {
                Method setCreatTime = entity.getClass().getDeclaredMethod("setCreatTime", LocalDateTime.class);
                Method setCreatTUser = entity.getClass().getDeclaredMethod("setCreatUser", Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                setCreatTime.invoke(entity,now);
                setUpdateTime.invoke(entity,now);
                setCreatTUser.invoke(entity,currentId);
                setUpdateUser.invoke(entity,currentId);
            } catch ( Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operationType == OperationType.UPDATE) {
            //为两个字段赋值
            try {

                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch ( Exception e) {
                throw new RuntimeException(e);
            }
        }


    }

}
