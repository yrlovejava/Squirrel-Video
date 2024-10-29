package com.squirrel.exception;

/**
 * Lua 脚本执行异常类
 */
public class LuaExecuteException extends CustomException{

    public LuaExecuteException(){
        super("Lua脚本执行失败了");
    }

    public LuaExecuteException(String message){
        super(message);
    }
}
