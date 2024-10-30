package com.squirrel.clients;

import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 交互服务客户端接口
 */
@FeignClient("squirrel-interact")
public interface IInteractClient {

    /**
     * 获取关注总数
     * @param userId 用户id
     * @return 关注总数
     */
    @GetMapping("/azaz/interact/follow/num")
    ResponseResult<Integer> getFollowNum(@RequestParam("userId") Long userId);

    /**
     * 获取粉丝总数
     * @param userId 用户id
     * @return 粉丝总数
     */
    @GetMapping("/azaz/interact/fansNum")
    ResponseResult<Integer> getFansNum(@RequestParam("userId") Long userId);

    /**
     * 查询两个用户是否相互关注
     * @param dto 两个用户的信息
     * @return ResponseResult 是否相互关注
     */
    @GetMapping("/azaz/follow/eachOther")
    ResponseResult<Boolean> isFollowEachOther(FollowEachOtherDTO dto);
}
