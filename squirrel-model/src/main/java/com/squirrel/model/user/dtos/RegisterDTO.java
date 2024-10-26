package com.squirrel.model.user.dtos;

import lombok.Data;

/**
 * 注册的 DTO
 */
@Data
public class RegisterDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;
}
