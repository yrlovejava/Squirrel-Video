package com.squirrel.exception;

import com.squirrel.constant.ResponseConstant;

/**
 * 用户未登录异常类
 */
public class UserNotLoginException extends CustomException{

    public UserNotLoginException() {
        super(ResponseConstant.NEED_LOGIN);
    }

}
