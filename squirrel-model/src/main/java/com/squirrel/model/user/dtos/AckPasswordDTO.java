package com.squirrel.model.user.dtos;

import lombok.Data;

/**
 * 用户密码传输对象
 */
@Data
public class AckPasswordDTO {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户密码
     */
    private String password;
}
