package com.squirrel.controller;

import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.follow.dtos.UserFollowDTO;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserPersonalInfoVO;
import com.squirrel.service.UserFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    /**
     * 获取互关列表朋友列表
     * @return ResponseResult 互关朋友列表
     */
    @GetMapping("/friends")
    public ResponseResult<List<UserPersonalInfoVO>> getFriends() {
        return userFollowService.getFriends();
    }

    /**
     * 获取关注列表
     * @return ResponseResult 关注列表
     */
    @GetMapping("/list")
    public ResponseResult<List<UserPersonalInfoVO>> getFollowList(){
        return userFollowService.getFollowList();
    }

    /**
     * 是否相互关注
     * @param firstUser 第一个用户id
     * @param secondUser 第二个用户id
     * @return ResponseResult<Boolean> 是否相互关注
     */
    @GetMapping("/ifFollowEachOther")
    public ResponseResult<Boolean> isFollowEachOther(@RequestParam("firstUser") Long firstUser,
                                                     @RequestParam("secondUser") Long secondUser) {
        return userFollowService.isFollowEachOther(firstUser,secondUser);
    }
}
