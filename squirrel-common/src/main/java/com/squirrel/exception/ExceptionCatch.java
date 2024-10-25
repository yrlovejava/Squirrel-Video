package com.squirrel.exception;

import com.squirrel.enums.AppHttpCodeEnum;
import com.squirrel.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class ExceptionCatch {

    /**
     * 处理不可控异常
     * @param e 异常
     * @return ResponseResult
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseResult exception(Exception e) {
        log.error("catch exception:{}",e.getMessage());
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }

    /**
     * 处理可控异常，自定义异常类
     * @param e 异常
     * @return ResponseResult
     */
    @ResponseBody
    @ExceptionHandler(CustomException.class)
    public ResponseResult exception(CustomException e){
        log.error("catch exception:{}",e.toString());
        return ResponseResult.errorResult(e.getAppHttpCodeEnum());
    }
}
