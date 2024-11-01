package com.squirrel.model.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户个人信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPersonInfoDTO {

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
     * 用户签名
     */
    private String signature;
}
