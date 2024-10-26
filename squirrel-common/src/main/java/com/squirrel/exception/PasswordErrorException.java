package com.squirrel.exception;

import com.squirrel.constant.ResponseConstant;

/**
 * 密码错误异常类
 */
public class PasswordErrorException extends CustomException{

    public PasswordErrorException() {
        super(ResponseConstant.PASSWORD_ERROR);
    }

    public PasswordErrorException(String msg) {
        super(msg);
    }
}
