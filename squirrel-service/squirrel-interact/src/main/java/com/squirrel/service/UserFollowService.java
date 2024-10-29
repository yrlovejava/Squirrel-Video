package com.squirrel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.follow.dtos.UserFollowDTO;
import com.squirrel.model.follow.pojos.Follow;
import com.squirrel.model.response.ResponseResult;

/**
 * 关注操作接口
 */
public interface UserFollowService extends IService<Follow> {

    /**
     * 是否相互关注
     * @param dto 是否相互关注的 dto
     * @return ResponseResult 是否相互关注
     */
    ResponseResult<Boolean> isFollowEachOther(FollowEachOtherDTO dto);

    /**
     * 关注操作
     * @param dto 关注 dto
     * @return ResponseResult 关注操作
     */
    ResponseResult follow(UserFollowDTO dto);
}
