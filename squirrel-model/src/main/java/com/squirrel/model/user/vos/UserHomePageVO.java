package com.squirrel.model.user.vos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户主页信息 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserHomePageVO {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 头像地址
     */
    private String username;

    /**
     * 签名
     */
    private String signature;

    /**
     * 粉丝数
     */
    private Integer fansNum;

    /**
     * 关注数
     */
    private Integer followNum;

    /**
     * 作品数
     */
    private Integer workNum;

    /**
     * 获赞数
     */
    private Integer likedNum;

    /**
     * 是否关注
     */
    private Boolean isFollow;
}
