package com.squirrel.exception;

/**
 * 数据库操作异常类
 */
public class DbOperationException extends CustomException{

    public DbOperationException(String msg) {
        super(msg);
    }

    public DbOperationException() {
        super("数据库操作异常");
    }
}
