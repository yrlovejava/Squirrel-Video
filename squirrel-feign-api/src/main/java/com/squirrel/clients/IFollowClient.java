package com.squirrel.clients;

import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 关注操作的远程调用接口
 */
@FeignClient("squirrel-interact")
public interface IFollowClient {

    /**
     * 查询两个用户是否相互关注
     * @param dto 两个用户的信息
     * @return ResponseResult 是否相互关注
     */
    @GetMapping("/azaz/follow/eachOther")
    ResponseResult<Boolean> isFollowEachOther(FollowEachOtherDTO dto);
}
