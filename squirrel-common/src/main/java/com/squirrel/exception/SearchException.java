package com.squirrel.exception;

/**
 * 搜索服务异常
 */
public class SearchException extends CustomException{

    public SearchException(String message) {
        super(message);
    }

    public SearchException(){
        super("搜索出错");
    }
}
