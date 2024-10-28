package com.squirrel.feign;

import com.squirrel.clients.IFollowClient;
import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.service.FollowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 关注操作远程调用接口的实现类
 */
@RestController
public class FollowClient implements IFollowClient {

    @Resource
    private FollowService followService;

    /**
     * 是否相互关注
     * @param dto 两个用户的信息
     * @return ResponseResult 是否相互关注
     */
    @Override
    @GetMapping("/azaz/follow/eachOther")
    public ResponseResult<Boolean> isFollowEachOther(FollowEachOtherDTO dto) {
        return followService.isFollowEachOther(dto);
    }
}
