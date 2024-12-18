package com.squirrel.clients;

import com.squirrel.interceptor.MyFeignRequestInterceptor;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserPersonalInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * 用户服务客户端远程接口
 */
@FeignClient(value = "squirrel-user",configuration = MyFeignRequestInterceptor.class)
public interface IUserClient {

    /**
     * 获取用户个人信息
     * @param userId 用户id
     * @return ResponseResult
     */
    @GetMapping("/azaz/user/feign/personal")
    ResponseResult<UserPersonalInfoVO> getUserPersonInfo(@RequestParam("userId") Long userId);

    /**
     * 批量获取用户个人信息
     * @param ids 用户id集合
     * @return ResponseResult 个人信息列表
     */
    @GetMapping("/azaz/user/feign/personals")
    ResponseResult<List<UserPersonalInfoVO>> getUserPersonInfos(Set<String> ids);
}
