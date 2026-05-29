package com.campus.trade.handler;

import com.campus.trade.constant.MessageConstant;
import com.campus.trade.exception.BaseException;
import com.campus.trade.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice  // 声明为全局异常处理器，用于捕获和处理Controller层抛出的异常
@Slf4j  // Lombok注解，用于自动生成日志记录器
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex 捕获的BaseException异常对象
     * @return 返回Result对象，包含错误信息
     */
    @ExceptionHandler  // 声明此方法用于处理BaseException类型的异常
    public Result exceptionHandler(BaseException ex){
        log.error("业务异常：{}", ex.getMessage(), ex);  // 记录业务异常日志
        return Result.error(ex.getMessage());  // 返回错误信息
    }
    
    /**
     * 捕获SQL约束异常
     * @param ex 捕获的SQLIntegrityConstraintViolationException异常对象
     * @return 返回Result对象，包含具体的错误信息
     */
    @ExceptionHandler  // 声明此方法用于处理SQLIntegrityConstraintViolationException类型的异常
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("数据库约束异常：{}", ex.getMessage(), ex);  // 记录数据库约束异常日志

        String message = ex.getMessage();  // 获取异常信息

      if(message.contains("Duplicate entry")){  // 判断是否为重复数据异常
          String[] s = message.split(" ");  // 分割异常信息
          String msg= s[2]+ MessageConstant.ALREADY_EXISTS;  // 拼装重复数据提示信息
          log.warn("重复数据：{}", msg);  // 记录重复数据警告日志
          return Result.error((msg));  // 返回重复数据错误信息
      }else {
          log.error("未知数据库异常：{}", message);  // 记录未知数据库异常日志
          return Result.error(MessageConstant.UNKNOWN_ERROR);  // 返回未知错误信息
      }
    }
    
    /**
     * 捕获所有未处理的异常
     * @param ex 捕获的Exception异常对象
     * @return 返回Result对象，包含系统未知错误信息
     */
    @ExceptionHandler(Exception.class)  // 声明此方法用于处理所有类型的异常
    public Result exceptionHandler(Exception ex){
        log.error("系统异常：{}", ex.getMessage(), ex);  // 记录系统异常日志
        return Result.error(MessageConstant.UNKNOWN_ERROR);  // 返回系统未知错误信息
    }

}
