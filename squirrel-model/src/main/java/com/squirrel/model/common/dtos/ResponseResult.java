package com.squirrel.model.common.dtos;

import com.squirrel.enums.AppHttpCodeEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 通用的结果返回类
 * @param <T> 泛型
 */
@Data
public class ResponseResult<T> implements Serializable {

    /**
     * 服务器地址
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
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.message = msg;
        responseResult.code = 1;
        return responseResult;
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

    /**
     * 成功 不返回数据
     * @return Result
     */
    public static ResponseResult successResult() {
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.code = 2;
        return responseResult;
    }

    public static <T> ResponseResult<T> successResult(T data) {
        ResponseResult<T> responseResult = new ResponseResult<T>();
        responseResult.data = data;
        responseResult.code = 2;
        return responseResult;
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

    public String getErrorMessage(){
        return message;
    }

    public void setErrorMessage(String errorMessage) {
        this.message = errorMessage;
    }


}
