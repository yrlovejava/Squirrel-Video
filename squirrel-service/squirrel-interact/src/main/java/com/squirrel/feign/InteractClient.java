package com.squirrel.feign;

import com.squirrel.clients.IInteractClient;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.service.UserFollowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * 判断是否关注
     * @param firstUser 第一个用户
     * @param senderUser 第二个用户
     * @return 是否关注
     */
    @GetMapping("/azaz/interact/follow/isFollow")
    public ResponseResult<Boolean> isFollow(@RequestParam("firstUser")Long firstUser,
                                            @RequestParam("senderUser")Long senderUser) {
        return userFollowService.isFollow(firstUser,senderUser);
    }
}
