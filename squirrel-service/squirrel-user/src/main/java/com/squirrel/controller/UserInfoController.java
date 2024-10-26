package com.squirrel.controller;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.dtos.UserPersonInfoDTO;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import com.squirrel.service.UserInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户信息控制器
 */
@RestController
@RequestMapping("/azaz/user")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取用户个人信息
     *
     * @param  userId 用户id
     * @return ResponseResult<UserPersonalInfoVo> 用户个人信息
     */
    @GetMapping("/personal")
    public ResponseResult<UserPersonInfoVO> getUserPersonInfo(Long userId) {
        return userInfoService.getUserPersonInfo(userId);
    }

    /**
     * 更新用户个人信息
     * @param dto 用户个人信息
     * @return 更新结果
     */
    @PutMapping("/personal")
    public ResponseResult updateUserPersonInfo(UserPersonInfoDTO dto){
        return userInfoService.updateUserPersonInfo(dto);
    }
}
