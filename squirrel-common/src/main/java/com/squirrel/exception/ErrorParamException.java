package com.squirrel.exception;

import com.squirrel.constant.ResponseConstant;

/**
 * 参数错误异常类
 */
public class ErrorParamException extends CustomException{

    public ErrorParamException() {
        super(ResponseConstant.PARAM_INVALID);
    }

    public ErrorParamException(String msg) {
        super(msg);
    }

}
