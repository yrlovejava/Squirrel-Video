package com.squirrel.exception;

/**
 * 操作异常
 */
public class ErrorOperationException extends CustomException{

    public ErrorOperationException() {
        super("操作异常");
    }

    public ErrorOperationException(String message) {
        super(message);
    }
}
