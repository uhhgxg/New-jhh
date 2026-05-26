package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("业务异常：{}", ex.getMessage(), ex);
        return Result.error(ex.getMessage());
    }
    
    /**
     * 捕获SQL约束异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("数据库约束异常：{}", ex.getMessage(), ex);

        String message = ex.getMessage();

      if(message.contains("Duplicate entry")){
          String[] s = message.split(" ");
          String msg= s[2]+ MessageConstant.ALREADY_EXISTS;
          log.warn("重复数据：{}", msg);
          return Result.error((msg));
      }else {
          log.error("未知数据库异常：{}", message);
          return Result.error(MessageConstant.UNKNOWN_ERROR);
      }
    }
    
    /**
     * 捕获所有未处理的异常
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception ex){
        log.error("系统异常：{}", ex.getMessage(), ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}
