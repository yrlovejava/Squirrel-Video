package com.squirrel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.follow.pojos.Follow;
import com.squirrel.model.response.ResponseResult;

/**
 * 关注操作接口
 */
public interface FollowService extends IService<Follow> {

    /**
     * 是否相互关注
     * @param dto 是否相互关注的 dto
     * @return ResponseResult 是否相互关注
     */
    ResponseResult<Boolean> isFollowEachOther(FollowEachOtherDTO dto);
}
