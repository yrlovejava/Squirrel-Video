package com.squirrel.exception;

import lombok.Getter;

/**
 * 自定义异常类，手动抛出异常
 */
@Getter
public class CustomException extends RuntimeException{

    public CustomException(String msg){
        super(msg);
    }

}
