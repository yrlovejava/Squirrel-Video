package com.squirrel.model.follow.dtos;

import lombok.Data;

/**
 * 关注 dto
 */
@Data
public class UserFollowDTO {

    /**
     * 操作目标用户id
     */
    private Long userId;

    /**
     * 操作类型
     * 1 关注
     * 0 取消关注
     */
    private Integer type;
}
