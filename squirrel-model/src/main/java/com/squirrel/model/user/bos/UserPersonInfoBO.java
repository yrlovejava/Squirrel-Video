package com.squirrel.model.user.bos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户个人信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPersonInfoBO {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户个人信息
     */
    private String username;

    /**
     * 用户头像
     */
    private String image;

    /**
     * 用户个性签名
     */
    private String signature;
}
