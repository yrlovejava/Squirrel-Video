package com.squirrel.clients;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务客户端远程接口
 */
@FeignClient("squirrel-user")
public interface IUserClient {

    /**
     * 获取用户个人信息
     * @param userId 用户id
     * @return ResponseResult
     */
    @GetMapping("/azaz/user/feign/personal")
    ResponseResult<UserPersonInfoVO> getUserPersonInfo(@RequestParam("userId") Long userId);

}
