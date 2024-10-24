package com.squirrel.model;

import com.squirrel.enums.AppHttpCodeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用的结果返回类
 * @param <T> 泛型
 */
@Data
public class ResponseResult<T> implements Serializable {

    /**
     * 主机地址
     */
    private String host;

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
     * 默认构造器返回 200
     */
    public ResponseResult() {
        this.code = 200;
    }

    public ResponseResult(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public ResponseResult(Integer code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public ResponseResult(Integer code, String msg) {
        this.code = code;
        this.message = msg;
    }

    /**
     * 错误结果
     * @param code 状态码
     * @param msg 提示信息
     * @return ResponseResult
     */
    public static ResponseResult errorResult(int code, String msg) {
        ResponseResult result = new ResponseResult();
        return result.error(code, msg);
    }

    /**
     * 成功结果
     * @param code 状态码
     * @param msg 提示信息
     * @return ResponseResult
     */
    public static ResponseResult successResult(int code, String msg) {
        ResponseResult result = new ResponseResult();
        return result.success(code,null,msg);
    }

    public static ResponseResult successResult(Object data) {
        ResponseResult result = setAppHttpCodeEnum(AppHttpCodeEnum.SUCCESS, AppHttpCodeEnum.SUCCESS.getErrorMessage());
        if(data!=null) {
            result.setData(data);
        }
        return result;
    }

    public static ResponseResult errorResult(AppHttpCodeEnum enums){
        return setAppHttpCodeEnum(enums,enums.getErrorMessage());
    }

    public static ResponseResult errorResult(AppHttpCodeEnum enums, String errorMessage){
        return setAppHttpCodeEnum(enums,errorMessage);
    }

    public static ResponseResult setAppHttpCodeEnum(AppHttpCodeEnum enums){
        return successResult(enums.getCode(),enums.getErrorMessage());
    }

    private static ResponseResult setAppHttpCodeEnum(AppHttpCodeEnum enums, String errorMessage){
        return successResult(enums.getCode(),errorMessage);
    }

    public ResponseResult<?> error(Integer code, String msg) {
        this.code = code;
        this.message = msg;
        return this;
    }

    public ResponseResult<?> success(Integer code, T data) {
        this.code = code;
        this.data = data;
        return this;
    }

    public ResponseResult<?> success(Integer code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.message = msg;
        return this;
    }

    public ResponseResult<?> success(T data) {
        this.data = data;
        return this;
    }
}
