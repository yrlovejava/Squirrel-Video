package com.squirrel.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的结果返回类
 * @param <T> 泛型
 */
@Data
public class ResponseResult<T> implements Serializable {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 额外的数据
     */
    private T data;

    /**
     * 成功 不返回数据
     * @return Result
     */
    public static ResponseResult successResult() {
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.code = 2;
        return responseResult;
    }

    /**
     * 成功 返回数据
     * @param object 返回数据
     * @return Result
     */
    public static <T> ResponseResult<T> successResult(T object) {
        ResponseResult<T> responseResult = new ResponseResult<T>();
        responseResult.data = object;
        responseResult.code = 2;
        return responseResult;
    }

    /**
     * 失败 返回错误信息,不返回数据
     * @param msg 错误信息
     * @return Result
     */
    public static ResponseResult errorResult(String msg) {
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.message = msg;
        responseResult.code = 1;
        return responseResult;
    }


}
