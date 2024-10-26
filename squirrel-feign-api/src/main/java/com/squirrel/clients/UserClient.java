package com.squirrel.clients;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.dtos.UserPersonInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 用户服务客户端远程接口
 */
@FeignClient("squirrel-user")
public interface UserClient {

    /**
     * 获取用户个人信息
     * @param userId 用户id
     * @return ResponseResult
     */
    @GetMapping("/azaz/user/personal")
    ResponseResult<UserPersonInfoDTO> getUserPersonInfo(Long userId);

}
