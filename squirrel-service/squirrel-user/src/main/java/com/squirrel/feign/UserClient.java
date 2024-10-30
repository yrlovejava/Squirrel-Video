package com.squirrel.feign;

import com.squirrel.clients.IUserClient;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import com.squirrel.service.UserInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 用户远程接口的实现
 */
@RestController
public class UserClient implements IUserClient {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取用户信息
     * @param userId 用户id
     * @return ResponseResult 用户信息
     */
    @Override
    @GetMapping("/azaz/user/feign/personal")
    public ResponseResult<UserPersonInfoVO> getUserPersonInfo(@RequestParam("userId") Long userId) {
        return userInfoService.getUserPersonInfo(userId);
    }

    /**
     * 批量获取用户个人信息
     * @param ids 用户id集合
     * @return ResponseResult 个人信息列表
     */
    @Override
    public ResponseResult<List<UserPersonInfoVO>> getUserPersonInfos(Set<String> ids) {
        return userInfoService.getUserPersonInfos(ids);
    }
}
