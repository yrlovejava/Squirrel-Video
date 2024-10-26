package com.squirrel.exception;

import com.squirrel.constant.ResponseConstant;

/**
 * 用户不存在异常类
 */
public class UserNotExitedException extends CustomException{

    public UserNotExitedException() {
        super(ResponseConstant.USER_NOT_EXIST);
    }

    public UserNotExitedException(String message) {
        super(message);
    }
}
