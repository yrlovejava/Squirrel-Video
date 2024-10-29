package com.squirrel.controller;

import com.squirrel.model.follow.dtos.UserFollowDTO;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.service.UserFollowService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户关注控制器
 */
@RestController
@RequestMapping("/azaz/interact/follow")
public class UserFollowController {

    @Resource
    private UserFollowService userFollowService;

    /**
     * 关注或者取消关注
     * @param dto 关注信息
     * @return ResponseResult 关注结果
     */
    @PostMapping("/do")
    public ResponseResult doFollow(UserFollowDTO dto){
        return userFollowService.follow(dto);
    }
}
