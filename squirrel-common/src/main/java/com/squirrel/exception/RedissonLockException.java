package com.squirrel.exception;

/**
 * redisson 分布式异常类
 */
public class RedissonLockException extends CustomException{

    public RedissonLockException(){
        super("redisson加锁失败");
    }

    public RedissonLockException(String message){
        super(message);
    }
}
