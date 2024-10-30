package com.squirrel.model.user.vos;

import lombok.Data;

/**
 * 用户个人信息VO
 */
@Data
public class UserPersonInfoVO {

    /**
     * 用户id
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String image;

    /**
     * 用户个人签名
     */
    private String signature;
}
