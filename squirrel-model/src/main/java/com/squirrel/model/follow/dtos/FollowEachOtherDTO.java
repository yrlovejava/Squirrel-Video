package com.squirrel.model.follow.dtos;

import lombok.Data;

/**
 * 是否相互关注的 dto
 */
@Data
public class FollowEachOtherDTO {

    /**
     * 第一个用户 id
     */
    private Long firstUserId;

    /**
     * 第二个用户 id
     */
    private Long secondUserId;
}
