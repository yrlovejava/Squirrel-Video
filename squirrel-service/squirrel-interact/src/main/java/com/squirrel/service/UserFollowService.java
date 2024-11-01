package com.squirrel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.follow.dtos.UserFollowDTO;
import com.squirrel.model.follow.pojos.Follow;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserPersonalInfoVO;

import java.util.List;

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

    /**
     * 判断是否关注
     * @param firstUserId 第一个用户
     * @param secondUserId 第二个用户
     * @return ResponseResult 是否关注
     */
    ResponseResult<Boolean> isFollow(Long firstUserId, Long secondUserId);

    /**
     * 获取互关朋友列表
     * @return ResponseResult 互关朋友列表
     */
    ResponseResult<List<UserPersonalInfoVO>> getFriends();

    /**
     * 获取关注列表
     * @return ResponseResult 关注列表
     */
    ResponseResult<List<UserPersonalInfoVO>> getFollowList();

    /**
     * 获取关注总数
     * @param userId 用户id
     * @return ResponseResult 关注总数
     */
    ResponseResult<Integer> getFollowNum(Long userId);

    /**
     * 获取粉丝数
     * @param userId 用户id
     * @return ResponseResult 粉丝数
     */
    ResponseResult<Integer> getFansNum(Long userId);
}
