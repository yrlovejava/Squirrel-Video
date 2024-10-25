package com.squirrel.model.user.dtos;

import lombok.Data;

/**
 * 登录 DTO
 */
@Data
public class LoginDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;
}
