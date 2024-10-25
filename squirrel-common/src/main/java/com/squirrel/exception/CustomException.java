package com.squirrel.exception;

import com.squirrel.enums.AppHttpCodeEnum;

/**
 * 自定义异常类，手动抛出异常
 */
public class CustomException extends RuntimeException{

    /**
     * http 状态码
     */
    private AppHttpCodeEnum appHttpCodeEnum;

    public CustomException(AppHttpCodeEnum appHttpCodeEnum) {
        this.appHttpCodeEnum = appHttpCodeEnum;
    }

    public AppHttpCodeEnum getAppHttpCodeEnum() {
        return this.appHttpCodeEnum;
    }
}
