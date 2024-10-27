package com.squirrel.exception;

/**
 * 七牛云上传异常类
 */
public class QiniuException extends CustomException{

    public QiniuException(){
        super("七牛云上传文件异常");
    }

    public QiniuException(String message){
        super(message);
    }
}
