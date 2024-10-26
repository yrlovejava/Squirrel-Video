package com.squirrel.exception;

import com.squirrel.constant.ResponseConstant;

/**
 * 空参数错误异常类
 */
public class NullParamException extends CustomException{

    public NullParamException() {
        super(ResponseConstant.PARAM_REQUIRE);
    }

    public NullParamException(String msg) {
        super(msg);
    }
}
