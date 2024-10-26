package com.squirrel.model.user.vos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户登录的VO
 */
@Data
@AllArgsConstructor
public class UserLoginVO {

    /**
     * token
     */
    private String token;

    /**
     * 用户id
     */
    private String userId;
}
