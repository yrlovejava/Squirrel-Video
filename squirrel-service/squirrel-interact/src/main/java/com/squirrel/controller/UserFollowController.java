package com.squirrel.controller;

import com.squirrel.model.follow.dtos.UserFollowDTO;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserPersonInfoVO;
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
     * 判断是否关注
     * @param firstUser 第一个用户
     * @param senderUser 第二个用户
     * @return 是否关注
     */
    @GetMapping("/isFollow")
    public ResponseResult<Boolean> isFollow(@RequestParam("firstUser")Long firstUser,
                                            @RequestParam("senderUser")Long senderUser) {
        return userFollowService.isFollow(firstUser,senderUser);
    }

    /**
     * 获取互关列表朋友列表
     * @return ResponseResult 互关朋友列表
     */
    @GetMapping("/friends")
    public ResponseResult<List<UserPersonInfoVO>> getFriends() {
        return userFollowService.getFriends();
    }

    /**
     * 获取关注列表
     * @return ResponseResult 关注列表
     */
    @GetMapping("/list")
    public ResponseResult<List<UserPersonInfoVO>> getFollowList(){
        return userFollowService.getFollowList();
    }
}
