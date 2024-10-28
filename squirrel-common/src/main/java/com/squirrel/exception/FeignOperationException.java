package com.squirrel.exception;

/**
 * 远程调用异常类
 */
public class FeignOperationException extends CustomException{

    public FeignOperationException() {
        super("远程调用失败");
    }

    public FeignOperationException(String msg) {
        super(msg);
    }
}
