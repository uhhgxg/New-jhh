package com.campus.trade.aspect;

import com.campus.trade.annotation.AutoFill;
import com.campus.trade.constant.AutoFillConstant;
import com.campus.trade.context.BaseContext;
import com.campus.trade.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自动填充切面类
 * 用于在数据库操作前后自动填充公共字段，如创建时间、更新时间、创建人、修改人等
 */
@Component
@Aspect
@Slf4j
public class AutoFillAspect {
    /**
     * 定义切点：拦截所有带有@AutoFill注解的mapper方法
     * 拦截范围：com.campus.trade.mapper包下的所有类的所有方法
     */
    @Pointcut("execution(* com.campus.trade.mapper.*.*(..)) && @annotation(com.campus.trade.annotation.AutoFill)")
    public void mapperPointcut() {
    }

    /**
     * 前置通知：在方法执行前进行公共字段的自动填充
     * @param joinPoint 连接点，可以获取目标方法的信息和参数
     */
    @Before("mapperPointcut()")
    public void before(JoinPoint joinPoint) {
        // 打印日志，表示开始进行公共字段填充
        log.info("开始进行公共字段填充");


        // 获取方法签名和方法对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 获取方法上的@AutoFill注解，从而确定操作类型（INSERT或UPDATE）
        AutoFill annotation = method.getAnnotation(AutoFill.class);
        OperationType operationType = annotation.value();
        log.info("当前操作类型：{}", operationType);
        // 获取方法参数
        Object[] args = joinPoint.getArgs();

        // 参数校验
        if (args == null || args.length == 0) {
            return;
        }
        // 获取实体对象（通常是参数列表中的第一个参数）
        Object entity = args[0];

        // 获取当前时间和当前用户ID
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        // 根据操作类型执行不同的字段填充逻辑
        if (operationType == OperationType.INSERT) {
            // 插入操作：需要填充创建时间、创建人、更新时间、更新人
            try {
                // 使用反射获取并调用setter方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                // 设置字段值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);


            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            // 更新操作：只需要填充更新时间和更新人
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}


