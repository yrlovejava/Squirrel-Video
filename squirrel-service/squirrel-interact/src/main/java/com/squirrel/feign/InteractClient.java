package com.squirrel.feign;

import com.squirrel.clients.IInteractClient;
import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.service.UserFollowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 交互操作远程调用接口的实现类
 */
@RestController
public class InteractClient implements IInteractClient {

    @Resource
    private UserFollowService userFollowService;

    /**
     * 获取关注总数
     * @param userId 用户id
     * @return 关注总数
     */
    @Override
    @GetMapping("/azaz/interact/follow/num")
    public ResponseResult<Integer> getFollowNum(Long userId) {
        return userFollowService.getFollowNum(userId);
    }

    /**
     * 获取粉丝总数
     * @param userId 用户id
     * @return 粉丝总数
     */
    @Override
    @GetMapping("/azaz/interact/fansNum")
    public ResponseResult<Integer> getFansNum(Long userId) {
        return userFollowService.getFansNum(userId);
    }

    /**
     * 是否相互关注
     * @param dto 两个用户的信息
     * @return ResponseResult 是否相互关注
     */
    @Override
    @GetMapping("/azaz/follow/eachOther")
    public ResponseResult<Boolean> isFollowEachOther(FollowEachOtherDTO dto) {
        return userFollowService.isFollowEachOther(dto);
    }
}
