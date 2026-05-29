package com.campus.trade.annotation;

import com.campus.trade.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识需要进行自动填充的方法
 * 该注解可以用于方法上，在运行时生效
 */
@Target({ElementType.METHOD})  // 指定注解可以使用的位置，这里表示只能用于方法上
@Retention(RetentionPolicy.RUNTIME)  // 指定注解的生命周期，表示在运行时仍然存在
// 自动填充注解 - 用于标识需要自动填充的方法

public @interface AutoFill {
    // 操作类型 - 枚举类型，用于标识具体的操作类型（如INSERT、UPDATE等）
    OperationType value();
}
