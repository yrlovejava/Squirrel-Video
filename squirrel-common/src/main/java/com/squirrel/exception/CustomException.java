package com.squirrel.exception;

import com.squirrel.enums.AppHttpCodeEnum;
import lombok.Getter;

/**
 * 自定义异常类，手动抛出异常
 */
@Getter
public class CustomException extends RuntimeException{

    /**
     * http 状态码
     */
    private final AppHttpCodeEnum appHttpCodeEnum;

    public CustomException(AppHttpCodeEnum appHttpCodeEnum) {
        this.appHttpCodeEnum = appHttpCodeEnum;
    }

}
