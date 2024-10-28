package com.squirrel.model.follow.dtos;

import com.squirrel.model.common.annotation.IdEncrypt;
import lombok.Data;

/**
 * 是否相互关注的 dto
 */
@Data
public class FollowEachOtherDTO {

    /**
     * 第一个用户 id
     */
    @IdEncrypt
    private Long firstUserId;

    /**
     * 第二个用户 id
     */
    @IdEncrypt
    private Long secondUserId;
}
